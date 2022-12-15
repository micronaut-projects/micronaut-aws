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
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.bind.BeanPropertyBinder;
import io.micronaut.core.convert.exceptions.ConversionErrorException;
import io.micronaut.core.convert.value.ConvertibleValues;
import io.micronaut.core.convert.value.ConvertibleValuesMap;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArgumentUtils;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.function.aws.HandlerUtils;
import io.micronaut.function.aws.LambdaApplicationContextBuilder;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.annotation.Status;
import io.micronaut.http.bind.RequestBinderRegistry;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.RouteExecutor;
import io.micronaut.http.server.binding.RequestArgumentSatisfier;
import io.micronaut.http.server.exceptions.response.ErrorResponseProcessor;
import io.micronaut.inject.qualifiers.Qualifiers;
import io.micronaut.jackson.codec.JsonMediaTypeCodec;
import io.micronaut.scheduling.executor.ExecutorSelector;
import io.micronaut.web.router.RouteInfo;
import io.micronaut.web.router.Router;
import io.micronaut.web.router.resource.StaticResourceResolver;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.function.BiFunction;

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
    private final BeanPropertyBinder beanPropertyBinder;
    private ApplicationContext applicationContext;
    private RequestArgumentSatisfier requestArgumentSatisfier;
    private StaticResourceResolver resourceResolver;
    private Router router;
    private ErrorResponseProcessor errorResponseProcessor;
    private RouteExecutor routeExecutor;
    final Map<MediaType, BiFunction<Argument<?>, String, Optional<Object>>> mediaTypeBodyDecoder = new HashMap<>();

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
        this.beanPropertyBinder = this.applicationContext.getBean(BeanPropertyBinder.class);
        populateMediaTypeBodyDecoders();
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

    @Override
    protected void handleRequest(
            MicronautAwsProxyRequest<?> containerRequest,
            MicronautAwsProxyResponse<?> containerResponse,
            Context lambdaContext) {
        Timer.start(TIMER_REQUEST);
        HandlerUtils.configureWithContext(this, lambdaContext);

        try {
            new AwsRequestLifecycle(this, routeExecutor, containerRequest, containerResponse).run()
                .onComplete((r, t) -> {
                    if (t == null) {
                        toAwsProxyResponse(containerResponse, r);
                    } else {
                        // should never happen, it should be transformed to an error response with onError
                        LOG.warn("Error in response flow");
                    }
                    containerResponse.close();
                });
        } finally {
            Timer.stop(TIMER_REQUEST);
        }
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

    private void populateMediaTypeBodyDecoders() {
        mediaTypeBodyDecoder.put(MediaType.APPLICATION_JSON_TYPE, this::getJsonDecodedBody);
        mediaTypeBodyDecoder.put(MediaType.APPLICATION_FORM_URLENCODED_TYPE, this::getFormUrlEncodedDecodedBody);
    }

    @NonNull
    private Optional<Object> getFormUrlEncodedDecodedBody(@Nullable Argument<?> bodyArgument,
                                                          @NonNull String body) {
        if (bodyArgument == null) {
            JsonNode encodedValues = lambdaContainerEnvironment.getObjectMapper().valueToTree(formUrlEncodedBodyToConvertibleValues(body));
            return Optional.ofNullable(encodedValues);
        }
        if (nestedBody(bodyArgument)) {
            return Optional.ofNullable(formUrlEncodedBodyToConvertibleValues(body));
        }
        return bindFormUrlEncoded(bodyArgument, body);
    }

    @NonNull
    private Optional<Object> getJsonDecodedBody(@Nullable Argument<?> bodyArgument,
                                                @NonNull String body) {

        if (bodyArgument == null) {
            JsonMediaTypeCodec jsonCodec = lambdaContainerEnvironment.getJsonCodec();
            JsonNode decoded = jsonCodec.decode(JsonNode.class, body);
            return Optional.of(decoded);
        }
        JsonMediaTypeCodec jsonCodec = lambdaContainerEnvironment.getJsonCodec();
        if (nestedBody(bodyArgument)) {
            return Optional.of(new ConvertibleValuesMap(jsonCodec.decode(Argument.of(Map.class), body)));
        }
        return Optional.of(jsonCodec.decode(bodyArgument, body));
    }

    @NonNull
    private static Optional<Map<String, List<String>>> formUrlEncodedBodyToMap(@NonNull String body) {
        QueryStringDecoder decoder = new QueryStringDecoder(body, false);
        Map<String, List<String>> parameters = decoder.parameters();
        return CollectionUtils.isEmpty(parameters) ? Optional.empty() :
            Optional.of(parameters);
    }

    @Nullable
    private static ConvertibleValues<?> formUrlEncodedBodyToConvertibleValues(@NonNull String body) {
        return formUrlEncodedBodyToMap(body)
            .map(ConvertibleValuesMap::new)
            .orElse(null);
    }

    private static boolean nestedBody(@NonNull Argument<?> bodyArgument) {
        AnnotationMetadata annotationMetadata = bodyArgument.getAnnotationMetadata();
        if (annotationMetadata.hasAnnotation(Body.class)) {
            return annotationMetadata.stringValue(Body.class).isPresent();
        }
        return false;
    }

    @NonNull
    private Optional<Object> bindFormUrlEncoded(@NonNull Argument<?> argument, @NonNull String formUrlEncodedString) {
        return formUrlEncodedBodyToMap(formUrlEncodedString)
            .flatMap(parameters -> bindFormUrlEncoded(argument, parameters));
    }

    @NonNull
    private Optional<Object> bindFormUrlEncoded(@NonNull Argument<?> argument, @NonNull Map<String, List<String>> bodyParameters) {
        Map<CharSequence, Object> source = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : bodyParameters.entrySet()) {
            source.put(entry.getKey(), entry.getValue());
        }
        try {
            return Optional.of(beanPropertyBinder.bind(argument.getType(), source));
        } catch (ConversionErrorException e) {
            if (LOG.isErrorEnabled()) {
                LOG.error("Unable to convert to {}", argument.getType().getSimpleName(), e);
            }
            return Optional.empty();
        }
    }

    private void applyRouteConfig(MicronautAwsProxyResponse<?> containerResponse, RouteInfo<?> finalRoute) {
        if (!containerResponse.getContentType().isPresent()) {
            finalRoute.getAnnotationMetadata().getValue(Produces.class, String.class).ifPresent(containerResponse::contentType);
        }
        finalRoute.getAnnotationMetadata().getValue(Status.class, HttpStatus.class).ifPresent(httpStatus -> containerResponse.status(httpStatus));
    }

    @Override
    public void close() {
        this.applicationContext.close();
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
}
