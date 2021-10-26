/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.function.aws.proxy;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.AwsProxySecurityContextWriter;
import com.amazonaws.serverless.proxy.internal.jaxrs.AwsProxySecurityContext;
import com.amazonaws.serverless.proxy.internal.testutils.Timer;
import com.amazonaws.serverless.proxy.model.*;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.core.annotation.AnnotationMetadata;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.async.subscriber.CompletionAwareSubscriber;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.TypeVariableResolver;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.function.aws.LambdaApplicationContextBuilder;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.bind.RequestBinderRegistry;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.filter.HttpFilter;
import io.micronaut.http.filter.HttpServerFilter;
import io.micronaut.http.filter.ServerFilterChain;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.RouteExecutor;
import io.micronaut.http.server.binding.RequestArgumentSatisfier;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import io.micronaut.http.server.types.files.StreamedFile;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.jackson.codec.JsonMediaTypeCodec;
import io.micronaut.scheduling.executor.ExecutorSelector;
import io.micronaut.web.router.*;
import io.micronaut.web.router.resource.StaticResourceResolver;
import org.apache.commons.io.IOUtils;
import org.reactivestreams.Publisher;
import org.reactivestreams.Subscriber;
import org.reactivestreams.Subscription;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.io.Closeable;
import java.io.InputStream;
import java.lang.reflect.UndeclaredThrowableException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Main entry for AWS API proxy with Micronaut.
 *
 * @author graemerocher
 * @since 1.1
 */
@TypeHint(
        accessType = {TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS, TypeHint.AccessType.ALL_PUBLIC},
        value = {
                AlbContext.class,
                ApiGatewayAuthorizerContext.class,
                ApiGatewayRequestIdentity.class,
                AwsProxyRequest.class,
                AwsProxyRequestContext.class,
                AwsProxyResponse.class,
                CognitoAuthorizerClaims.class,
                ContainerConfig.class,
                ErrorModel.class,
                Headers.class,
                MultiValuedTreeMap.class,
                AwsProxySecurityContext.class
        }
)
public final class MicronautLambdaContainerHandler
        extends AbstractLambdaContainerHandler<AwsProxyRequest, AwsProxyResponse, MicronautAwsProxyRequest<?>, MicronautAwsProxyResponse<?>> implements ApplicationContextProvider, Closeable, AutoCloseable {

    private static final Logger LOG = LoggerFactory.getLogger(MicronautLambdaContainerHandler.class);
    private static final String TIMER_INIT = "MICRONAUT_COLD_START";
    private static final String TIMER_REQUEST = "MICRONAUT_HANDLE_REQUEST";
    private final ApplicationContextBuilder applicationContextBuilder;
    private final LambdaContainerState lambdaContainerEnvironment;
    private ApplicationContext applicationContext;
    private RequestArgumentSatisfier requestArgumentSatisfier;
    private StaticResourceResolver resourceResolver;
    private Router router;
    private ErrorResponseProcessor errorResponseProcessor;
    private RouteExecutor routeExecutor;

    /**
     * Default constructor.
     *
     * @param applicationContextBuilder The context builder
     * @throws ContainerInitializationException The exception
     */
    public MicronautLambdaContainerHandler(ApplicationContextBuilder applicationContextBuilder) throws ContainerInitializationException {
        this(new LambdaContainerState(), applicationContextBuilder, null);
    }

    /**
     * Default constructor.
     *
     * @throws ContainerInitializationException The exception
     */
    public MicronautLambdaContainerHandler() throws ContainerInitializationException {
        this(new LambdaContainerState(), ApplicationContext.builder(), null);
    }

    /**
     * Constructor used to inject a preexisting {@link ApplicationContext}.
     * @param applicationContext application context
     *
     * @throws ContainerInitializationException The exception
     */
    public MicronautLambdaContainerHandler(ApplicationContext applicationContext) throws ContainerInitializationException {
        this(new LambdaContainerState(), ApplicationContext.builder(), applicationContext);
    }

    /**
     * constructor.
     *
     * @param lambdaContainerEnvironment The container environment
     * @param applicationContextBuilder  The context builder
     * @throws ContainerInitializationException if the container couldn't be started
     */
    private MicronautLambdaContainerHandler(
            LambdaContainerState lambdaContainerEnvironment,
            ApplicationContextBuilder applicationContextBuilder,
            ApplicationContext applicationContext) throws ContainerInitializationException {
        super(
                AwsProxyRequest.class,
                AwsProxyResponse.class,
                new MicronautRequestReader(lambdaContainerEnvironment),
                new MicronautResponseWriter(lambdaContainerEnvironment),
                new AwsProxySecurityContextWriter(),
                new MicronautAwsProxyExceptionHandler(lambdaContainerEnvironment)

        );
        ArgumentUtils.requireNonNull("applicationContextBuilder", applicationContextBuilder);
        this.lambdaContainerEnvironment = lambdaContainerEnvironment;
        this.applicationContextBuilder = applicationContextBuilder;

        if (applicationContext == null) {
            initialize();
        } else {
            this.applicationContext = applicationContext;
            initContainerState();
        }
    }


    /**
     * constructor.
     *
     * @param lambdaContainerEnvironment The environment
     * @throws ContainerInitializationException if the container couldn't be started
     */
    private MicronautLambdaContainerHandler(LambdaContainerState lambdaContainerEnvironment) throws ContainerInitializationException {
        this(lambdaContainerEnvironment, ApplicationContext.builder(), null);
    }

    /**
     * @return The underlying application context
     */
    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    protected ObjectMapper objectMapper() {
        return lambdaContainerEnvironment.getObjectMapper();
    }

    @Override
    protected ObjectWriter writerFor(Class<AwsProxyResponse> responseClass) {
        return objectMapper().writerFor(responseClass);
    }

    @Override
    protected ObjectReader readerFor(Class<AwsProxyRequest> requestClass) {
        return objectMapper().readerFor(requestClass);
    }

    @Override
    protected MicronautAwsProxyResponse<?> getContainerResponse(MicronautAwsProxyRequest<?> request, CountDownLatch latch) {
        MicronautAwsProxyResponse response = new MicronautAwsProxyResponse(
                request.getAwsProxyRequest(),
                latch,
                lambdaContainerEnvironment
        );

        Optional<Object> routeMatchAttr = request.getAttribute(HttpAttributes.ROUTE_MATCH);
        routeMatchAttr.ifPresent(o -> response.setAttribute(HttpAttributes.ROUTE_MATCH, o));

        request.setResponse(response);

        return request.getResponse();
    }

    @Override
    public void initialize() throws ContainerInitializationException {
        Timer.start(TIMER_INIT);
        try {
            LambdaApplicationContextBuilder.setLambdaConfiguration(applicationContextBuilder);
            this.applicationContext = applicationContextBuilder.build().start();
            initContainerState();
        } catch (Exception e) {
            throw new ContainerInitializationException(
                    "Error starting Micronaut container: " + e.getMessage(),
                    e
            );
        }
        Timer.stop(TIMER_INIT);
    }

    protected void initContainerState() {
        this.lambdaContainerEnvironment.setApplicationContext(applicationContext);
        this.lambdaContainerEnvironment.setJsonCodec(applicationContext.getBean(JsonMediaTypeCodec.class));
        this.lambdaContainerEnvironment.setRouter(applicationContext.getBean(Router.class));

        Optional<ObjectMapper> objectMapper = applicationContext.findBean(ObjectMapper.class, Qualifiers.byName("aws"));
        if (objectMapper.isPresent()) {
            lambdaContainerEnvironment.setObjectMapper(objectMapper.get());
        } else {
            lambdaContainerEnvironment.setObjectMapper(applicationContext.getBean(ObjectMapper.class));
        }

        this.requestArgumentSatisfier = new RequestArgumentSatisfier(
                applicationContext.getBean(RequestBinderRegistry.class)
        );
        this.resourceResolver = applicationContext.getBean(StaticResourceResolver.class);
        addConverters();

        this.router = lambdaContainerEnvironment.getRouter();
        this.errorResponseProcessor = applicationContext.getBean(ErrorResponseProcessor.class);
        HttpServerConfiguration serverConfiguration = applicationContext.getBean(HttpServerConfiguration.class);
        ExecutorSelector executorSelector = applicationContext.getBean(ExecutorSelector.class);

        this.routeExecutor = new RouteExecutor(
                this.router,
                applicationContext,
                requestArgumentSatisfier,
                serverConfiguration,
                errorResponseProcessor,
                executorSelector
        );

    }

    /**
     * Add converters to the Application environment.
     */
    protected void addConverters() {
        addByteArrayToStringConverter();
    }

    /**
     * Adds a converter from byte array to string.
     */
    protected void addByteArrayToStringConverter() {
        applicationContext.getEnvironment().addConverter(
                byte[].class, String.class, bytes -> new String(bytes, StandardCharsets.UTF_8)
        );
    }

    // Returns true if handled here
    private boolean handlePossibleErrorStatus(
            MicronautAwsProxyRequest<?> request,
            MicronautAwsProxyResponse<?> response) {

        // TODO/NOTE: Copied from RoutingInBoundHandler

        final HttpMethod httpMethod = request.getMethod();
        final String requestPath = request.getUri().getPath();
        final MediaType contentType = request.getContentType().orElse(null);
        final String requestMethodName = request.getMethodName();

        // if there is no route present try to locate a route that matches a different HTTP method
        final List<UriRouteMatch<?, ?>> anyMatchingRoutes = router
                .findAny(request.getPath(), request)
                .collect(Collectors.toList());
        final Collection<MediaType> acceptedTypes = request.accept();
        final boolean hasAcceptHeader = CollectionUtils.isNotEmpty(acceptedTypes);

        Set<MediaType> acceptableContentTypes = contentType != null ? new HashSet<>(5) : null;
        Set<String> allowedMethods = new HashSet<>(5);
        Set<MediaType> produceableContentTypes = hasAcceptHeader ? new HashSet<>(5) : null;
        for (UriRouteMatch<?, ?> anyRoute : anyMatchingRoutes) {
            final String routeMethod = anyRoute.getRoute().getHttpMethodName();
            if (!requestMethodName.equals(routeMethod)) {
                allowedMethods.add(routeMethod);
            }
            if (contentType != null && !anyRoute.doesConsume(contentType)) {
                acceptableContentTypes.addAll(anyRoute.getRoute().getConsumes());
            }
            if (hasAcceptHeader && !anyRoute.doesProduce(acceptedTypes)) {
                produceableContentTypes.addAll(anyRoute.getRoute().getProduces());
            }
        }

        if (CollectionUtils.isNotEmpty(acceptableContentTypes)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Content type not allowed for URI {}, method {}, and content type {}", request.getUri(),
                        requestMethodName, contentType);
            }

            handleStatusError(
                    request,
                    response,
                    HttpResponse.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE),
                    "Content Type [" + contentType + "] not allowed. Allowed types: " + acceptableContentTypes);
            return true;
        }

        if (CollectionUtils.isNotEmpty(produceableContentTypes)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Content type not allowed for URI {}, method {}, and content type {}", request.getUri(),
                        requestMethodName, contentType);
            }

            handleStatusError(
                    request,
                    response,
                    HttpResponse.status(HttpStatus.NOT_ACCEPTABLE),
                    "Specified Accept Types " + acceptedTypes + " not supported. Supported types: " + produceableContentTypes);
            return true;
        }

        if (!allowedMethods.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Method not allowed for URI {} and method {}", request.getUri(), requestMethodName);
            }

            handleStatusError(
                    request,
                    response,
                    HttpResponse.notAllowedGeneric(allowedMethods),
                    "Method [" + requestMethodName + "] not allowed for URI [" + request.getUri() + "]. Allowed methods: " + allowedMethods);
            return true;
        }
        return false;
    }

    private void newHandleRequest(
            MicronautAwsProxyRequest<?> request,
            MicronautAwsProxyResponse<?> response) {

        final HttpMethod httpMethod = request.getMethod();
        final String requestPath = request.getUri().getPath();
        final MediaType contentType = request.getContentType().orElse(null);
        final String requestMethodName = request.getMethodName();

        // ROUTE_MATCH set by MicronautRequestReader
        Optional<UriRouteMatch> routeMatch = request.getAttribute(HttpAttributes.ROUTE_MATCH, UriRouteMatch.class);

        if (routeMatch.isPresent()) {
            UriRouteMatch _routeMatch = routeMatch.get();
            // TODO Most of these already set?
            request.setAttribute(HttpAttributes.ROUTE, _routeMatch.getRoute());
            request.setAttribute(HttpAttributes.ROUTE_MATCH, _routeMatch);
            request.setAttribute(HttpAttributes.ROUTE_INFO, _routeMatch);
            request.setAttribute(HttpAttributes.URI_TEMPLATE, _routeMatch.getRoute().getUriMatchTemplate().toString());
        } else {
            if (!handlePossibleErrorStatus(request, response)) {
                // TODO
            }
            return;
        }

        // TODO if route.isWebSocketRoute

        handleRouteMatch(routeMatch.get(), request, response);
    }

    private void filterAndEncodeResponse(
            MicronautAwsProxyRequest<?> request,
            MicronautAwsProxyResponse<?> response,
            Publisher<MutableHttpResponse<?>> responsePublisher) {
        AtomicReference<HttpRequest<?>> requestReference = new AtomicReference<>(request);

        Flux.from(routeExecutor.filterPublisher(requestReference, responsePublisher))
                .contextWrite(ctx -> ctx.put(ServerRequestContext.KEY, request))
                .subscribe(new Subscriber<MutableHttpResponse<?>>() {
                    Subscription subscription;
                    @Override
                    public void onSubscribe(Subscription s) {
                        this.subscription = s;
                        s.request(1);
                    }

                    @Override
                    public void onNext(MutableHttpResponse<?> message) {
                        encodeHttpResponse(
                                request,
                                toAwsProxyResponse(response, message),
                                message.body()
                        );
                        subscription.request(1);
                    }

                    @Override
                    public void onError(Throwable t) {
                        final MutableHttpResponse<?> defaultErrorResponse = routeExecutor.createDefaultErrorResponse(request, t);
                        encodeHttpResponse(
                                request,
                                toAwsProxyResponse(response, defaultErrorResponse),
                                defaultErrorResponse.body()
                        );
                    }

                    @Override
                    public void onComplete() {
                    }
                });
    }

    private void handleStatusError(
        MicronautAwsProxyRequest<?> request,
        MicronautAwsProxyResponse<?> response,
        MutableHttpResponse<?> defaultResponse,
        String message) {

        Optional<RouteMatch<Object>> statusRoute = router.findStatusRoute(defaultResponse.status(), request);
        if (statusRoute.isPresent()) {
            handleRouteMatch(statusRoute.get(), request, response);
        } else {
            if (request.getMethod() != HttpMethod.HEAD) {
                defaultResponse = errorResponseProcessor.processResponse(
                        ErrorContext.builder(request).errorMessage(message).build(),
                        defaultResponse
                );
                if (!defaultResponse.getContentType().isPresent()) {
                    defaultResponse = defaultResponse.contentType(MediaType.APPLICATION_JSON_TYPE);
                }
            }

            filterAndEncodeResponse(request, response, Publishers.just(defaultResponse));
        }
    }

    private void handleRouteMatch(
            RouteMatch<?> originalRoute,
            MicronautAwsProxyRequest<?> request,
            MicronautAwsProxyResponse<?> response
    ) {
        final AnnotationMetadata annotationMetadata = originalRoute.getAnnotationMetadata();
        annotationMetadata.stringValue(Produces.class)
                .map(MediaType::new)
                .ifPresent(response::contentType);

        Flux<MutableHttpResponse<?>> routeResponse;
        try {
            decodeRequestBody(request, originalRoute);

            RouteMatch<?> route = requestArgumentSatisfier.fulfillArgumentRequirements(originalRoute, request, false);

            // TODO? body, streamed...

            Flux<RouteMatch<?>> routeMatchPublisher = Flux.just(route);

            routeResponse = routeExecutor.executeRoute(
                    request,
                    true,
                    routeMatchPublisher
            ).mapNotNull(r -> convertResponseBody(response, r.getAttribute(HttpAttributes.ROUTE_MATCH, RouteMatch.class).get(), r.body()).block());
        } catch (Exception e) {
            routeResponse = Flux.from(routeExecutor.filterPublisher(new AtomicReference<>(request), routeExecutor.onError(e, request)));
        }

        routeResponse
                .contextWrite(ctx -> ctx.put(ServerRequestContext.KEY, request))
                .subscribe(new CompletionAwareSubscriber<HttpResponse<?>>() {
                    @Override
                    protected void doOnSubscribe(Subscription subscription) {
                        subscription.request(1);
                    }

                    @Override
                    protected void doOnNext(HttpResponse<?> message) {
                        encodeHttpResponse(
                                request,
                                toAwsProxyResponse(response, message),
                                message.body()
                        );
                        subscription.request(1);
                    }

                    @Override
                    protected void doOnError(Throwable throwable) {
                        final MutableHttpResponse<?> defaultErrorResponse = routeExecutor.createDefaultErrorResponse(request, throwable);
                        encodeHttpResponse(
                                request,
                                toAwsProxyResponse(response, defaultErrorResponse),
                                defaultErrorResponse.body()
                        );
                    }

                    @Override
                    protected void doOnComplete() {
                    }
                });
    }

    private MicronautAwsProxyResponse<?> toAwsProxyResponse(
            MicronautAwsProxyResponse<?> response,
            HttpResponse<?> message) {

        if (response != message) {
            response.status(message.status(), message.status().getReason());
            response.body(message.body());
            message.getHeaders().forEach((name, value) -> {
                for (String val : value) {
                    response.header(name, val);
                }
            });
            response.getAttributes().putAll(message.getAttributes());
        }

        return response;
    }

    // TODO Not needed? Response is encoded/written in MicronautResponseWriter after handleRequest finishes
    //      So we just need a complete MicronautAwsProxyResponse
    private void encodeHttpResponse(
            MicronautAwsProxyRequest<?> request,
            MicronautAwsProxyResponse<?> response,
            Object body) {



        // TODO: Nothing?

//        boolean isHead = request.getMethod() == HttpMethod.HEAD;
//
//        if (isHead) {
//            // TODO HEAD
//        } else {
//
//        }
    }

    @Override
    protected void handleRequest(
            MicronautAwsProxyRequest<?> containerRequest,
            MicronautAwsProxyResponse<?> containerResponse,
            Context lambdaContext) {
        Timer.start(TIMER_REQUEST);

        try {
            if (true) {
                ServerRequestContext.with(containerRequest, () -> {
                    try {
                        newHandleRequest(containerRequest, containerResponse);
                    } finally {
                        containerResponse.close();
                    }
                });
            } else {
                // process filters & invoke servlet
                ServerRequestContext.with(containerRequest, () -> {
                    final Optional<UriRouteMatch> routeMatch = containerRequest.getAttribute(
                            HttpAttributes.ROUTE_MATCH,
                            UriRouteMatch.class
                    );

                    try {
                        if (routeMatch.isPresent()) {
                            final UriRouteMatch finalRoute = routeMatch.get();
                            containerRequest.setAttribute(
                                    HttpAttributes.ROUTE_MATCH, finalRoute
                            );

                            final AnnotationMetadata annotationMetadata = finalRoute.getAnnotationMetadata();
                            annotationMetadata.stringValue(Produces.class)
                                    .map(MediaType::new)
                                    .ifPresent(containerResponse::contentType);

                            final Mono<MutableHttpResponse<?>> responsePublisher = Mono.defer(() ->
                                    executeRoute(containerRequest, containerResponse, finalRoute)
                            );

                            final AtomicReference<HttpRequest<?>> requestReference = new AtomicReference<>(containerRequest);
                            final Mono<MutableHttpResponse<?>> filterPublisher = Mono.from(filterPublisher(
                                    requestReference,
                                    responsePublisher));

                            filterPublisher.onErrorResume(mayBeWrapped -> {
                                Throwable throwable = mayBeWrapped instanceof UndeclaredThrowableException ? mayBeWrapped.getCause() : mayBeWrapped;
                                final RouteMatch<Object> errorHandler = lambdaContainerEnvironment.getRouter().route(
                                        finalRoute.getDeclaringType(),
                                        throwable
                                ).orElseGet(() -> lambdaContainerEnvironment.getRouter().route(
                                        throwable
                                ).orElse(null));
                                if (errorHandler == null) {
                                    final ApplicationContext ctx = lambdaContainerEnvironment.getApplicationContext();
                                    final ExceptionHandler exceptionHandler = ctx
                                            .findBean(ExceptionHandler.class, Qualifiers.byTypeArgumentsClosest(
                                                    throwable.getClass(), Object.class
                                            )).orElse(null);

                                    if (exceptionHandler != null) {
                                        final Mono<? extends MutableHttpResponse<?>> errorFlowable =
                                                handleException(containerRequest, containerResponse, throwable, exceptionHandler);

                                        return filterPublisher(
                                                requestReference,
                                                errorFlowable
                                        );
                                    }
                                } else if (errorHandler instanceof MethodBasedRouteMatch) {
                                    final Publisher<? extends MutableHttpResponse<?>> errorPublisher = executeRoute(
                                            containerRequest,
                                            containerResponse,
                                            (MethodBasedRouteMatch) errorHandler
                                    );

//                                    return Mono.from(errorPublisher);
                                    return filterPublisher(
                                            requestReference,
                                            errorPublisher
                                    );
                                }
                                return Mono.error(throwable);
                            }).block();
                        } else {
                            final Optional<UriRouteMatch<Object, Object>> finalRoute = lambdaContainerEnvironment.getRouter().route(
                                    containerRequest.getMethod(),
                                    containerRequest.getPath()
                            );

                            if (finalRoute.isPresent()) {
                                final AnnotationMetadata annotationMetadata = finalRoute.get().getAnnotationMetadata();
                                annotationMetadata.stringValue(Produces.class).map(MediaType::new)
                                        .ifPresent(containerResponse::contentType);

                                final MediaType[] expectedContentType = Arrays.stream(annotationMetadata.stringValues(Consumes.class))
                                        .map(MediaType::new)
                                        .toArray(MediaType[]::new);
                                final MediaType requestContentType = containerRequest.getContentType().orElse(null);

                                if (expectedContentType.length > 0 && Arrays.stream(expectedContentType).noneMatch(ct -> ct.equals(requestContentType)) && Arrays.stream(expectedContentType).noneMatch(ct -> ct.equals(MediaType.ALL_TYPE))) {
                                    containerResponse.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE);
                                    containerResponse.close();
                                    return;
                                }
                            }

                            final Optional<URL> staticMatch = resourceResolver.resolve(containerRequest.getPath());
                            if (staticMatch.isPresent()) {
                                final StreamedFile streamedFile = new StreamedFile(staticMatch.get());
                                long length = streamedFile.getLength();
                                if (length > -1) {
                                    containerResponse.header(HttpHeaders.CONTENT_LENGTH, String.valueOf(length));
                                }
                                containerResponse.header(HttpHeaders.CONTENT_TYPE, streamedFile.getMediaType().toString());
                                try (InputStream inputStream = streamedFile.getInputStream()) {
                                    byte[] data = IOUtils.toByteArray(inputStream);
                                    ((MutableHttpResponse) containerResponse).body(data);
                                } catch (Throwable e) {
                                    final RouteMatch<Object> errorHandler = lambdaContainerEnvironment.getRouter().route(e).orElse(null);
                                    final AtomicReference<HttpRequest<?>> requestReference = new AtomicReference<>(containerRequest);
                                    final ApplicationContext ctx = lambdaContainerEnvironment.getApplicationContext();

                                    if (errorHandler instanceof MethodBasedRouteMatch) {
                                        executeRoute(containerRequest, containerResponse, (MethodBasedRouteMatch) errorHandler).block();
                                    } else {

                                        final ExceptionHandler exceptionHandler = ctx
                                                .findBean(ExceptionHandler.class, Qualifiers.byTypeArgumentsClosest(
                                                        e.getClass(), Object.class
                                                )).orElse(null);

                                        if (exceptionHandler != null) {
                                            final Mono<? extends MutableHttpResponse<?>> errorFlowable =
                                                    handleException(containerRequest, containerResponse, e, exceptionHandler);

                                            filterPublisher(
                                                    requestReference,
                                                    errorFlowable
                                            ).block();
                                        }
                                    }
                                }
                            } else {
                                final AtomicReference<HttpRequest<?>> requestReference = new AtomicReference<>(containerRequest);
                                final Stream<UriRouteMatch<Object, Object>> matches = lambdaContainerEnvironment
                                        .getRouter()
                                        .findAny(containerRequest.getPath(), containerRequest);

                                final Mono<? extends MutableHttpResponse<?>> statusFlowable = Mono.fromCallable(() -> {
                                    containerResponse.status(matches.findFirst().isPresent() ? HttpStatus.METHOD_NOT_ALLOWED : HttpStatus.NOT_FOUND);
                                    return containerResponse;
                                });

                                filterPublisher(
                                        requestReference,
                                        statusFlowable
                                ).block();
                            }
                        }
                    } finally {
                        containerResponse.close();
                    }
                });
            }
        } finally {
            Timer.stop(TIMER_REQUEST);
        }
    }

    private void decodeRequestBody(MicronautAwsProxyRequest<?> containerRequest, RouteMatch<?> finalRoute) {
        if (!containerRequest.isBodyDecoded()) {
            final boolean permitsRequestBody = HttpMethod.permitsRequestBody(containerRequest.getMethod());
            if (permitsRequestBody) {
                final MediaType requestContentType = containerRequest.getContentType().orElse(null);
                if (requestContentType != null && requestContentType.getExtension().equalsIgnoreCase("json")) {
                    final MediaType[] expectedContentType = finalRoute.getAnnotationMetadata().getValue(Consumes.class, MediaType[].class).orElse(null);
                    if (expectedContentType == null || Arrays.stream(expectedContentType).anyMatch(ct -> ct.getExtension().equalsIgnoreCase("json"))) {
                        final Optional<String> body = containerRequest.getBody(String.class);
                        if (body.isPresent()) {

                            Argument<?> bodyArgument = finalRoute.getBodyArgument().orElse(null);
                            if (bodyArgument == null) {
                                //TODO evaluate this condition
                                if (finalRoute instanceof MethodBasedRouteMatch) {
                                    bodyArgument = Arrays.stream(((MethodBasedRouteMatch) finalRoute).getArguments())
                                            .filter(arg -> HttpRequest.class.isAssignableFrom(arg.getType()))
                                            .findFirst()
                                            .flatMap(TypeVariableResolver::getFirstTypeVariable).orElse(null);
                                }
                            }

                            if (bodyArgument != null) {
                                final Class<?> rawType = bodyArgument.getType();
                                if (Publishers.isConvertibleToPublisher(rawType) || HttpRequest.class.isAssignableFrom(rawType)) {
                                    bodyArgument = bodyArgument.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
                                }
                                final Object decoded = lambdaContainerEnvironment.getJsonCodec().decode(bodyArgument, body.get());
                                ((MicronautAwsProxyRequest) containerRequest)
                                        .setDecodedBody(decoded);
                            } else {
                                final JsonNode jsonNode = lambdaContainerEnvironment.getJsonCodec().decode(JsonNode.class, body.get());
                                ((MicronautAwsProxyRequest) containerRequest)
                                        .setDecodedBody(jsonNode);
                            }
                        }
                    }
                }
            }
        }
    }

    private Mono<? extends MutableHttpResponse<?>> handleException(MicronautAwsProxyRequest<?> containerRequest, MicronautAwsProxyResponse<?> containerResponse, Throwable throwable, ExceptionHandler exceptionHandler) {
        return Mono.fromCallable(() -> {
            Object result = exceptionHandler.handle(containerRequest, throwable);
            MutableHttpResponse<?> response = errorResultToResponse(result);
            containerResponse.status(response.getStatus());
            response.getContentType().ifPresent(containerResponse::contentType);
            response.getBody().ifPresent(((MutableHttpResponse) containerResponse)::body);
            return response;
        });
    }


    private Mono<MutableHttpResponse<?>> executeRoute(
            MicronautAwsProxyRequest<?> containerRequest,
            MicronautAwsProxyResponse<?> containerResponse,
            MethodBasedRouteMatch finalRoute) {
        try {
            decodeRequestBody(containerRequest, finalRoute);
        } catch (Exception e) {
            return Mono.error(e);
        }

        final RouteMatch<?> boundRoute = requestArgumentSatisfier.fulfillArgumentRequirements(
                finalRoute,
                containerRequest,
                false
        );

        Object result = boundRoute.execute();

        return createResponseForBody(containerResponse, boundRoute, result);
    }

    private Mono<MutableHttpResponse<?>> createResponseForBody(
            MicronautAwsProxyResponse<?> containerResponse,
            RouteMatch<?> routeMatch,
            Object result) {

        if (result instanceof Optional) {
            Optional<?> optional = (Optional) result;
            result = optional.orElse(null);
        }
        if (!void.class.isAssignableFrom(routeMatch.getReturnType().getType()) && result == null) {
            applyRouteConfig(containerResponse, routeMatch);
            containerResponse.status(HttpStatus.NOT_FOUND);
            return Mono.just(containerResponse);
        }
        return convertResponseBody(containerResponse, routeMatch, result);
    }

    private Mono<MutableHttpResponse<?>> convertResponseBody(
            MicronautAwsProxyResponse<?> containerResponse,
            RouteMatch<?> routeMatch,
            Object body) {
        if (Publishers.isConvertibleToPublisher(body)) {
            Mono<?> single;
            if (Publishers.isSingle(body.getClass()) || routeMatch.getReturnType().isSpecifiedSingle()) {
                single = Mono.from(Publishers.convertPublisher(body, Publisher.class));
            } else {
                single = Flux.from(Publishers.convertPublisher(body, Publisher.class)).collectList();
            }
            return single.map((Function<Object, MutableHttpResponse<?>>) o -> {
                if (!(o instanceof MicronautAwsProxyResponse)) {
                    ((MutableHttpResponse) containerResponse).body(o);
                }
                applyRouteConfig(containerResponse, routeMatch);
                return containerResponse;
            });
        } else {
            if (!(body instanceof MicronautAwsProxyResponse)) {
                applyRouteConfig(containerResponse, routeMatch);
                ((MutableHttpResponse) containerResponse).body(body);
            }
            return Mono.just(containerResponse);
        }
    }

    private MutableHttpResponse<?> errorResultToResponse(Object result) {
        MutableHttpResponse<?> response;
        if (result == null) {
            response = io.micronaut.http.HttpResponse.serverError();
        } else if (result instanceof io.micronaut.http.HttpResponse) {
            response = (MutableHttpResponse) result;
        } else {
            response = io.micronaut.http.HttpResponse.serverError()
                    .body(result);
            MediaType.fromType(result.getClass()).ifPresent(response::contentType);
        }
        return response;
    }

    private void applyRouteConfig(MicronautAwsProxyResponse<?> containerResponse, RouteMatch<?> finalRoute) {
        if (!containerResponse.getContentType().isPresent()) {
            finalRoute.getAnnotationMetadata().getValue(Produces.class, String.class).ifPresent(containerResponse::contentType);
        }
        finalRoute.getAnnotationMetadata().getValue(Status.class, HttpStatus.class).ifPresent(httpStatus -> containerResponse.status(httpStatus));
    }

    @Override
    public void close() {
        this.applicationContext.close();
    }

    private Mono<MutableHttpResponse<?>> filterPublisher(
            AtomicReference<HttpRequest<?>> requestReference,
            Publisher<? extends MutableHttpResponse<?>> routePublisher) {
        Publisher<? extends io.micronaut.http.MutableHttpResponse<?>> finalPublisher;
        List<HttpFilter> filters = new ArrayList<>(lambdaContainerEnvironment.getRouter().findFilters(requestReference.get()));
        if (!filters.isEmpty()) {
            // make the action executor the last filter in the chain
            filters.add((HttpServerFilter) (req, chain) -> (Publisher<MutableHttpResponse<?>>) routePublisher);

            AtomicInteger integer = new AtomicInteger();
            int len = filters.size();
            ServerFilterChain filterChain = new LambdaFilterChain(integer, len, filters, requestReference);
            HttpFilter httpFilter = filters.get(0);
            Publisher<? extends HttpResponse<?>> resultingPublisher = httpFilter.doFilter(requestReference.get(), filterChain);
            finalPublisher = (Publisher<MutableHttpResponse<?>>) resultingPublisher;
        } else {
            finalPublisher = routePublisher;
        }

        return Mono.from(finalPublisher);
    }

    /**
     * Holds state for the running container.
     */
    private static class LambdaContainerState implements MicronautLambdaContainerContext {
        private Router router;
        private ApplicationContext applicationContext;
        private JsonMediaTypeCodec jsonCodec;
        private ObjectMapper objectMapper;

        @Override
        public Router getRouter() {
            return router;
        }

        @Override
        public JsonMediaTypeCodec getJsonCodec() {
            return jsonCodec;
        }

        @Override
        public ApplicationContext getApplicationContext() {
            return applicationContext;
        }

        @Override
        public ObjectMapper getObjectMapper() {
            return objectMapper;
        }

        void setJsonCodec(JsonMediaTypeCodec jsonCodec) {
            this.jsonCodec = jsonCodec;
        }

        void setRouter(Router router) {
            this.router = router;
        }

        void setApplicationContext(ApplicationContext applicationContext) {
            this.applicationContext = applicationContext;
        }

        void setObjectMapper(ObjectMapper objectMapper) {
            this.objectMapper = objectMapper;
        }

    }

    /**
     * Implementation of {@link ServerFilterChain} for Lambda.
     */
    private static class LambdaFilterChain implements ServerFilterChain {
        private final AtomicInteger integer;
        private final int len;
        private final List<HttpFilter> filters;
        private final AtomicReference<HttpRequest<?>> requestReference;

        LambdaFilterChain(
                AtomicInteger integer,
                int len,
                List<HttpFilter> filters,
                AtomicReference<HttpRequest<?>> requestReference) {
            this.integer = integer;
            this.len = len;
            this.filters = filters;
            this.requestReference = requestReference;
        }

        @SuppressWarnings("unchecked")
        @Override
        public Publisher<MutableHttpResponse<?>> proceed(HttpRequest<?> request) {
            int pos = integer.incrementAndGet();
            if (pos > len) {
                throw new IllegalStateException("The FilterChain.proceed(..) method should be invoked exactly once per filter execution. The method has instead been invoked multiple times by an erroneous filter definition.");
            }
            HttpFilter httpFilter = filters.get(pos);
            return (Publisher<MutableHttpResponse<?>>) httpFilter.doFilter(requestReference.getAndSet(request), this);
        }
    }
}
