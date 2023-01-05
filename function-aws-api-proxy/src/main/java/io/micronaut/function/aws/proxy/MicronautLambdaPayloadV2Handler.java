/*
 * Copyright 2017-2023 original authors
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

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Base64;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;
import java.util.function.Supplier;
import java.util.stream.Collectors;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.internal.testutils.Timer;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.io.buffer.ByteBuffer;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.function.aws.HandlerUtils;
import io.micronaut.function.aws.LambdaApplicationContextBuilder;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpRequestFactory;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.bind.RequestBinderRegistry;
import io.micronaut.http.codec.MediaTypeCodec;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.RouteExecutor;
import io.micronaut.http.server.binding.RequestArgumentSatisfier;
import io.micronaut.http.server.exceptions.response.ErrorContext;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import io.micronaut.http.uri.UriBuilder;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.jackson.codec.JsonMediaTypeCodec;
import io.micronaut.scheduling.executor.ExecutorSelector;
import io.micronaut.web.router.RouteMatch;
import io.micronaut.web.router.Router;
import io.micronaut.web.router.UriRouteMatch;
import io.micronaut.web.router.resource.StaticResourceResolver;
import jdk.internal.joptsimple.internal.Strings;
import org.reactivestreams.Publisher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@TypeHint(
    accessType = {TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS, TypeHint.AccessType.ALL_PUBLIC},
    value = {
        APIGatewayV2HTTPEvent.class,
        APIGatewayV2HTTPEvent.RequestContext.class,
    }
)
public class MicronautLambdaPayloadV2Handler implements RequestHandler<APIGatewayV2HTTPEvent,
    APIGatewayV2HTTPResponse>, ApplicationContextProvider {
    private static final Logger LOG = LoggerFactory.getLogger(MicronautLambdaPayloadV2Handler.class);
    private static final String TIMER_INIT = "MICRONAUT_COLD_START";
    private static final String TIMER_REQUEST = "MICRONAUT_HANDLE_REQUEST";
    private final ApplicationContextBuilder applicationContextBuilder;

    private ApplicationContext applicationContext;

    private LambdaContainerState lambdaContainerEnvironment;
    private RequestArgumentSatisfier requestArgumentSatisfier;
    private StaticResourceResolver resourceResolver;
    private Router router;
    private ErrorResponseProcessor errorResponseProcessor;
    private RouteExecutor routeExecutor;
    private HttpRequestFactory requestFactory;

    public MicronautLambdaPayloadV2Handler() throws ContainerInitializationException {
        this(ApplicationContext.builder());
    }

    public MicronautLambdaPayloadV2Handler(ApplicationContextBuilder applicationContextBuilder) throws ContainerInitializationException {
        this.applicationContextBuilder = applicationContextBuilder;
        this.lambdaContainerEnvironment = new LambdaContainerState();
        initialize();
    }

    @Override
    public APIGatewayV2HTTPResponse handleRequest(final APIGatewayV2HTTPEvent event,
                                                  final Context context) {
        HttpRequest<?> request = toHttpRequest(event);

        Timer.start(TIMER_REQUEST);
        HandlerUtils.configureWithContext(this, context);

        try {
            HttpResponse<?> response = ServerRequestContext.with(request, (Supplier<HttpResponse<?>>) () -> {
                Optional<UriRouteMatch> routeMatch = request
                    .getAttribute(HttpAttributes.ROUTE_MATCH, UriRouteMatch.class);

                if (routeMatch.isPresent()) {
                    return handleRouteMatch(routeMatch.get(), request);
                }

                return handlePossibleErrorStatus(request);
            });

            return toResponse(response);
        } finally {
            Timer.stop(TIMER_REQUEST);
        }
    }

    private HttpResponse<?> handleRouteMatch(
        RouteMatch<?> originalRoute,
        HttpRequest<?> request
    ) {
        Flux<MutableHttpResponse<?>> routeResponse;
        try {
            RouteMatch<?> route = requestArgumentSatisfier.fulfillArgumentRequirements(originalRoute, request, false);

            Flux<RouteMatch<?>> routeMatchPublisher = Flux.just(route);

            routeResponse = routeExecutor.executeRoute(
                request,
                true,
                routeMatchPublisher);
        } catch (Exception e) {
            routeResponse = Flux.from(routeExecutor.filterPublisher(new AtomicReference<>(request), routeExecutor.onError(e, request)));
        }

        return routeResponse
            .contextWrite(ctx -> ctx.put(ServerRequestContext.KEY, request))
            .single()
            .onErrorResume(Throwable.class, e -> Mono
                .just(routeExecutor.createDefaultErrorResponse(request, e)))
            .block();
    }

    private APIGatewayV2HTTPResponse toResponse(HttpResponse<?> message) {
        Map<String, String> headers = message.getHeaders().asMap().entrySet().stream()
            .collect(Collectors.toMap(k -> k.getKey(), v -> Strings.join(v.getValue(), ",")));

        List<String> cookies = message.getCookies().asMap().entrySet().stream()
            .map(entry -> entry.getValue().toString())
            .collect(Collectors.toList());

        ResponseBody responseBody = encodeBody(message);

        APIGatewayV2HTTPResponse response = APIGatewayV2HTTPResponse.builder()
            .withBody(responseBody.getBody())
            .withIsBase64Encoded(responseBody.isBase64Encoded())
            .withCookies(cookies)
            .withHeaders(headers)
            .withStatusCode(message.getStatus().getCode())
            .build();

        return response;
    }

    static class ResponseBody {
        private String body;
        private boolean base64Encoded;

        public ResponseBody(final String body) {
            this(body, false);
        }

        public ResponseBody(final String body, final boolean base64Encoded) {
            this.body = body;
            this.base64Encoded = base64Encoded;
        }

        public String getBody() {
            return body;
        }

        public void setBody(final String body) {
            this.body = body;
        }

        public boolean isBase64Encoded() {
            return base64Encoded;
        }

        public void setBase64Encoded(final boolean base64Encoded) {
            this.base64Encoded = base64Encoded;
        }
    }

    private ResponseBody encodeBody(HttpResponse<?> response) {
        Object body = response.body();
        if (body instanceof CharSequence) {
            return new ResponseBody(body.toString());
        }

        byte[] encoded = encodeInternal(response, lambdaContainerEnvironment.getJsonCodec());
        if (encoded != null) {
            final String contentType = response.getContentType().map(MediaType::toString).orElse(null);
            if (!isBinary(contentType)) {
                return new ResponseBody(new String(encoded, response.getCharacterEncoding()));
            } else {
                return new ResponseBody(Base64.getMimeEncoder().encodeToString(encoded), true);
            }
        }
        return null;
    }

    /**
     * Is the content binary.
     * @param contentType The content type
     * @return True if it is
     */
    boolean isBinary(String contentType) {
        if (contentType != null) {
            int semidx = contentType.indexOf(';');
            if (semidx >= 0) {
                return MicronautLambdaContainerHandler.getContainerConfig().isBinaryContentType(contentType.substring(0, semidx));
            } else {
                return MicronautLambdaContainerHandler.getContainerConfig().isBinaryContentType(contentType);
            }
        }
        return false;
    }

    private byte[] encodeInternal(HttpResponse<?> response, MediaTypeCodec codec) {
        Object body = response.body();
        byte[] encoded = null;
        try {
            if (body != null) {
                if (body instanceof ByteBuffer) {
                    encoded = ((ByteBuffer) body).toByteArray();
                } else if (body instanceof byte[]) {
                    encoded = (byte[]) body;
                } else {
                    encoded = codec.encode(body);
                }
            }
        } catch (Exception e) {
            throw new RuntimeException("Invalid Response: " + e.getMessage() , e);
        }
        return encoded;
    }

    private HttpResponse<?> handlePossibleErrorStatus(HttpRequest<?> request) {
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

            return handleStatusError(
                request,
                HttpResponse.status(HttpStatus.UNSUPPORTED_MEDIA_TYPE),
                "Content Type [" + contentType + "] not allowed. Allowed types: " + acceptableContentTypes);
        }

        if (CollectionUtils.isNotEmpty(produceableContentTypes)) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Content type not allowed for URI {}, method {}, and content type {}", request.getUri(),
                    requestMethodName, contentType);
            }

            return handleStatusError(
                request,
                HttpResponse.status(HttpStatus.NOT_ACCEPTABLE),
                "Specified Accept Types " + acceptedTypes + " not supported. Supported types: " + produceableContentTypes);
        }

        if (!allowedMethods.isEmpty()) {
            if (LOG.isDebugEnabled()) {
                LOG.debug("Method not allowed for URI {} and method {}", request.getUri(), requestMethodName);
            }

            return handleStatusError(
                request,
                HttpResponse.notAllowedGeneric(allowedMethods),
                "Method [" + requestMethodName + "] not allowed for URI [" + request.getUri() + "]. Allowed methods: " + allowedMethods);
        }

        return handleStatusError(
            request,
            HttpResponse.notFound(),
            "Page Not Found");
    }

    private HttpResponse<?> handleStatusError(
        HttpRequest<?> request,
        MutableHttpResponse<?> defaultResponse,
        String message) {

        Optional<RouteMatch<Object>> statusRoute = router.findStatusRoute(defaultResponse.status(), request);
        if (statusRoute.isPresent()) {
            return handleRouteMatch(statusRoute.get(), request);
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

            return filterAndEncodeResponse(request, defaultResponse);
        }
    }

    private HttpResponse<?> filterAndEncodeResponse(
        HttpRequest<?> request,
        MutableHttpResponse<?> defaultResponse) {
        AtomicReference<HttpRequest<?>> requestReference = new AtomicReference<>(request);
        Publisher<MutableHttpResponse<?>> responsePublisher = Publishers.just(defaultResponse);
        return Mono.from(routeExecutor.filterPublisher(requestReference, responsePublisher))
            .contextWrite(ctx -> ctx.put(ServerRequestContext.KEY, request))
            .onErrorResume(Throwable.class, e -> Mono.just(routeExecutor.createDefaultErrorResponse(request, e)))
            .block();
    }

    private HttpRequest<?> toHttpRequest(final APIGatewayV2HTTPEvent event) {
        HttpMethod method = HttpMethod.parse(event.getRequestContext().getHttp().getMethod());
        UriBuilder uriBuilder = UriBuilder.of(event.getRawPath() + "?" + event.getRawQueryString());

        Router router;

        MutableHttpRequest<Object> request = requestFactory.create(method, uriBuilder.toString());

        event.getHeaders().entrySet().stream()
            .forEach(entry -> {
                String[] values = entry.getValue().split(",");
                Arrays.stream(values)
                    .forEach(headerValue -> request.header(entry.getKey(), headerValue));
            });

        event.getCookies().forEach(cookie -> {
            String[] cookieKeyValue = cookie.split("=", 1);
            request.cookie(Cookie.of(cookieKeyValue[0], cookieKeyValue[1]));
        });

        return request;
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
        applicationContext.getEnvironment().addConverter(
            byte[].class, String.class, bytes -> new String(bytes, StandardCharsets.UTF_8)
        );

        this.router = lambdaContainerEnvironment.getRouter();
        this.errorResponseProcessor = applicationContext.getBean(ErrorResponseProcessor.class);
        this.requestFactory = applicationContext.getBean(HttpRequestFactory.class);
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

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }
}
