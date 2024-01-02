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
package io.micronaut.function.aws.runtime;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaRuntime;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerRequestEvent;
import io.micronaut.aws.ua.UserAgentProvider;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.context.env.CommandLinePropertySource;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.cli.CommandLine;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.aws.MicronautLambdaContext;
import io.micronaut.function.aws.XRayUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.DefaultHttpClientConfiguration;
import io.micronaut.http.client.HttpClient;
import io.micronaut.json.JsonMapper;
import io.micronaut.logging.LogLevel;

import java.io.InputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

import static io.micronaut.core.annotation.TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS;
import static io.micronaut.core.annotation.TypeHint.AccessType.ALL_DECLARED_FIELDS;
import static io.micronaut.core.annotation.TypeHint.AccessType.ALL_DECLARED_METHODS;
import static io.micronaut.core.annotation.TypeHint.AccessType.ALL_PUBLIC;
import static io.micronaut.core.annotation.TypeHint.AccessType.ALL_PUBLIC_CONSTRUCTORS;
import static io.micronaut.core.annotation.TypeHint.AccessType.ALL_PUBLIC_FIELDS;
import static io.micronaut.core.annotation.TypeHint.AccessType.ALL_PUBLIC_METHODS;
import static io.micronaut.http.HttpHeaders.USER_AGENT;

/**
 * Class that can be used as a entry point for a AWS Lambda custom runtime.
 * @see <a href="https://docs.aws.amazon.com/lambda/latest/dg/runtimes-custom.html">Custom AWS Lambda runtimes</a>.
 *
 * @param <RequestType> The expected request object. This is the model class that the event JSON is de-serialized to
 * @param <ResponseType> The expected Lambda function response object. Responses will be written to this model object
 * @param <HandlerRequestType> The request type for {@link com.amazonaws.services.lambda.runtime.RequestHandler}.
 * @param <HandlerResponseType> The response type for the {@link com.amazonaws.services.lambda.runtime.RequestHandler}.
 *
 * @author sdelamo
 * @since 2.0.0
 */
@TypeHint(
    accessType = {
        ALL_PUBLIC,
        ALL_DECLARED_CONSTRUCTORS,
        ALL_PUBLIC_CONSTRUCTORS,
        ALL_DECLARED_METHODS,
        ALL_DECLARED_FIELDS,
        ALL_PUBLIC_METHODS,
        ALL_PUBLIC_FIELDS
    },
    value = {
        com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.RequestContext.Authorizer.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.RequestContext.Http.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.RequestContext.IAM.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.RequestContext.CognitoIdentity.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.RequestContext.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse.class,
        com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerRequestEvent.class,
        com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerResponseEvent.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.RequestIdentity.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent.class,
        com.amazonaws.services.lambda.runtime.events.ScheduledEvent.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent.class,
        com.amazonaws.services.lambda.runtime.events.CloudFrontEvent.class,
        com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent.class,
        com.amazonaws.services.lambda.runtime.events.CodeCommitEvent.class,
        com.amazonaws.services.lambda.runtime.events.CognitoEvent.class,
        com.amazonaws.services.lambda.runtime.events.ConfigEvent.class,
        com.amazonaws.services.lambda.runtime.events.IoTButtonEvent.class,
        com.amazonaws.services.lambda.runtime.events.LexEvent.class,
        com.amazonaws.services.lambda.runtime.events.SNSEvent.class,
        com.amazonaws.services.lambda.runtime.events.SQSEvent.class
    }
)
@SuppressWarnings("java:S119") // More descriptive generics are better here
public abstract class AbstractMicronautLambdaRuntime<RequestType, ResponseType, HandlerRequestType, HandlerResponseType>
    implements ApplicationContextProvider, AwsLambdaRuntimeApi {
    @Nullable
    protected String userAgent;

    protected Object handler;

    @SuppressWarnings("unchecked")
    protected final Class<RequestType> requestType = initTypeArgument(0);

    @SuppressWarnings("unchecked")
    protected final Class<ResponseType> responseType = initTypeArgument(1);

    @SuppressWarnings("unchecked")
    protected final Class<HandlerRequestType> handlerRequestType = initTypeArgument(2);

    @SuppressWarnings("unchecked")
    protected final Class<HandlerResponseType> handlerResponseType = initTypeArgument(3);

    /**
     * Constructor.
     */
    public AbstractMicronautLambdaRuntime() {
    }

    /**
     * Starts the runtime API event loop.
     * @param args Command line arguments
     * @throws MalformedURLException if the lambda endpoint URL is malformed
     **/
    public void run(String... args) throws MalformedURLException {
        final URL runtimeApiURL = lookupRuntimeApiEndpoint();
        logn(LogLevel.DEBUG, "runtime endpoint: ", runtimeApiURL);
        final Predicate<URL> loopUntil = url -> true;
        startRuntimeApiEventLoop(runtimeApiURL, loopUntil, args);
    }

    /**
     * Uses {@link UserAgentProvider} to populate {@link AbstractMicronautLambdaRuntime#userAgent}.
     */
    protected void populateUserAgent() {
        if (getApplicationContext().containsBean(UserAgentProvider.class)) {
            UserAgentProvider userAgentProvider = getApplicationContext().getBean(UserAgentProvider.class);
            this.userAgent = userAgentProvider.userAgent();
        }
    }

    /**
     *
     * @throws ConfigurationException if the handler is not of type RequestHandler or RequestStreamHandler
     */
    protected void validateHandler() throws ConfigurationException {
        if (handler == null) {
            throw new ConfigurationException("no handler instantiated. Override either createHandler() or createRequestStreamHandler() or annotate your Handler class with @Introspected");
        }
        if (!(handler instanceof RequestHandler || handler instanceof RequestStreamHandler)) {
            throw new ConfigurationException("handler must be of type com.amazonaws.services.lambda.runtime.RequestHandler or com.amazonaws.services.lambda.runtime.RequestStreamHandler");
        }
    }

    @Override
    public ApplicationContext getApplicationContext() {
        if (handler instanceof ApplicationContextProvider applicationContextProvider) {
            return applicationContextProvider.getApplicationContext();
        }
        return null;
    }

    /**
     * @param args command line arguments
     * @return An {@link ApplicationContextBuilder} with the command line arguments as a property source and the environment set to lambda
     */
    public ApplicationContextBuilder createApplicationContextBuilderWithArgs(String... args) {
        CommandLine commandLine = CommandLine.parse(args);
        return ApplicationContext.builder()
            .environments(MicronautLambdaContext.ENVIRONMENT_LAMBDA)
            .propertySources(new CommandLinePropertySource(commandLine));
    }

    /**
     *
     * @param args Command Line Args
     * @return a {@link RequestHandler} or {@code null}.
     */
    @Nullable
    protected RequestHandler<HandlerRequestType, HandlerResponseType> createRequestHandler(String... args) {
        return null;
    }

    /**
     *
     * @param args Command Line Args
     * @return a {@link RequestStreamHandler} or {@code null}.
     */
    @Nullable
    protected RequestStreamHandler createRequestStreamHandler(String... args) {
        return null;
    }

    /**
     *
     * @param args Command Line Args
     * @return if {@link AbstractMicronautLambdaRuntime#createHandler(String...)} or {@link AbstractMicronautLambdaRuntime#createRequestStreamHandler(String...)} are implemented, it returns the handler returned by those methods. If they are not, it attempts to instantiate the class
     * referenced by the environment variable {@link ReservedRuntimeEnvironmentVariables#HANDLER} via Introspection. Otherwise, it returns {@code null}.
     */
    @Nullable
    protected Object createHandler(String... args) {
        RequestHandler<HandlerRequestType, HandlerResponseType> requestHandler = createRequestHandler(args);
        if (requestHandler != null) {
            return requestHandler;
        }
        RequestStreamHandler requestStreamHandler = createRequestStreamHandler(args);
        if (requestStreamHandler != null) {
            return requestStreamHandler;
        }
        return createEnvironmentHandler();
    }

    /**
     * @return A Handler by instantiating the class referenced by the environment variable {@link ReservedRuntimeEnvironmentVariables#HANDLER} via Introspection. Otherwise, it returns {@code null}.
     */
    @Nullable
    protected Object createEnvironmentHandler() {
        String localHandler = getEnv(ReservedRuntimeEnvironmentVariables.HANDLER);
        logn(LogLevel.DEBUG, "Handler: ", localHandler);
        if (localHandler != null) {
            Optional<Class<?>> handlerClassOptional = parseHandlerClass(localHandler);
            logn(LogLevel.WARN, "No handler Class parsed for ", localHandler);
            if (handlerClassOptional.isPresent()) {
                log(LogLevel.DEBUG, "Handler Class parsed. Instantiating it via introspection\n");
                Class handlerClass = handlerClassOptional.get();
                final BeanIntrospection introspection = BeanIntrospection.getIntrospection(handlerClass);
                return introspection.instantiate();
            }
        }
        return null;
    }

    /**
     *
     * @param handler handler in format file.method, where file is the name of the file without an extension, and method is the name of a method or function that's defined in the file.
     * @return Empty or an Optional with the referenced class.
     */
    protected Optional<Class<?>> parseHandlerClass(@NonNull String handler) {
        String[] arr = handler.split("::");
        if (arr.length > 0) {
            return ClassUtils.forName(arr[0], null);
        }
        return Optional.empty();
    }

    /**
     *
     * @param handlerResponse Handler response object
     * @return If the handlerResponseType and the responseType are identical just returns the supplied object. However,
     * if the response type is of type {@link APIGatewayProxyResponseEvent} it attempts to serialized the handler response
     * as a JSON String and set it in the response body. If the object cannot be serialized, a 400 response is returned
     */
    @Nullable
    protected ResponseType createResponse(HandlerResponseType handlerResponse) {
        if (handlerResponseType == responseType) {
            log(LogLevel.TRACE, "HandlerResponseType and ResponseType are identical\n");
            return (ResponseType) handlerResponse;

        } else if (responseType == APIGatewayProxyResponseEvent.class || responseType == APIGatewayV2HTTPResponse.class) {
            log(LogLevel.TRACE, "response type is APIGatewayProxyResponseEvent\n");
            try {
                byte[] json = serializeAsByteArray(handlerResponse);
                if (json != null) {
                    return (ResponseType) respond(HttpStatus.OK, json, MediaType.APPLICATION_JSON);
                }
            } catch (IOException ignored) {
            }
            return (ResponseType) respond(HttpStatus.BAD_REQUEST, "Could not serialize response as json".getBytes(), MediaType.TEXT_PLAIN);
        }
        return null;
    }

    /**
     *
     * @param status HTTP Status of the response
     * @param body Body of the response
     * @param contentType HTTP Header Content-Type value
     * @return a {@link APIGatewayProxyResponseEvent} populated with the supplied status, body and content type
     */
    protected APIGatewayProxyResponseEvent respond(HttpStatus status, byte[] body, String contentType) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, contentType);
        response.setHeaders(headers);
        response.setIsBase64Encoded(true);
        response.setBody(Base64.getEncoder().encodeToString(body));
        response.setStatusCode(status.getCode());
        logn(LogLevel.TRACE, "response: ", status.getCode(), " content type: ", headers.get(HttpHeaders.CONTENT_TYPE), " message ", body);
        return response;
    }


    /**
     *
     * @param request Request obtained from the Runtime API
     * @return if the request and the handler type are the same, just return the request, if the request is of type {@link APIGatewayProxyRequestEvent} it attempts to build an object of type HandlerRequestType with the body of the request, else returns {@code null}
     * @throws IOException if underlying request body contains invalid content
     *   expected for result type (or has other mismatch issues)
     */
    @Nullable
    protected HandlerRequestType createHandlerRequest(RequestType request) throws IOException {
        if (requestType == handlerRequestType) {
            return (HandlerRequestType) request;
        } else if (request instanceof ApplicationLoadBalancerRequestEvent applicationLoadBalancerRequestEvent) {
            log(LogLevel.TRACE, "request of type ApplicationLoadBalancerRequestEvent");
            String content = applicationLoadBalancerRequestEvent.getBody();
            return valueFromContent(content, handlerRequestType);
        } else if (request instanceof APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent) {
            log(LogLevel.TRACE, "request of type APIGatewayProxyRequestEvent");
            String content = apiGatewayProxyRequestEvent.getBody();
            return valueFromContent(content, handlerRequestType);
        } else if (request instanceof APIGatewayV2HTTPEvent apiGatewayV2HTTPEvent) {
            log(LogLevel.TRACE, "request of type APIGatewayV2HTTPEvent\n");
            String content = apiGatewayV2HTTPEvent.getBody();
            return valueFromContent(content, handlerRequestType);
        }

        log(LogLevel.TRACE, "createHandlerRequest return null\n");
        return null;
    }

    /**
     * Creates a GET request for the {@value #NEXT_INVOCATION_URI} endpoint.
     * If a bean of type {@link UserAgentProvider} exists, it adds an HTTP Header User-Agent to the request.
     * @param userAgentProvider UseAgent Provider
     * @return a Mutable HTTP Request to the {@value #NEXT_INVOCATION_URI} endpoint.
     */
    @NonNull
    protected MutableHttpRequest createNextInvocationHttpRequest(@Nullable UserAgentProvider userAgentProvider) {
        MutableHttpRequest<?> nextInvocationHttpRequest = HttpRequest.GET(AwsLambdaRuntimeApi.NEXT_INVOCATION_URI);
        if (userAgentProvider != null) {
            nextInvocationHttpRequest.header(USER_AGENT, userAgentProvider.userAgent());
        }
        return nextInvocationHttpRequest;
    }

    /**
     * Starts the runtime API event loop.
     *
     * @param runtimeApiURL             The runtime API URL.
     * @param loopUntil                 A predicate that allows controlling when the event loop should exit
     * @param args                      Command Line arguments
     */
    protected void startRuntimeApiEventLoop(@NonNull URL runtimeApiURL,
                                            @NonNull Predicate<URL> loopUntil,
                                            String... args) {
        try {
            handler = createHandler(args);
            validateHandler();
            ApplicationContext applicationContext = getApplicationContext();
            if (applicationContext == null) {
                throw new ConfigurationException("Application Context is null");
            }
            UserAgentProvider userAgentProvider = applicationContext.findBean(UserAgentProvider.class).orElse(null);
            populateUserAgent();
            final DefaultHttpClientConfiguration config = new DefaultHttpClientConfiguration();
            config.setReadIdleTimeout(null);
            config.setReadTimeout(null);
            config.setConnectTimeout(null);
            try (HttpClient endpointClient = applicationContext.createBean(HttpClient.class, runtimeApiURL, config)) {
                final BlockingHttpClient blockingHttpClient = endpointClient.toBlocking();
                try {
                    while (loopUntil.test(runtimeApiURL)) {
                        MutableHttpRequest<?> nextInvocationHttpRequest = createNextInvocationHttpRequest(userAgentProvider);
                        if (handler instanceof RequestStreamHandler) {
                            handleInvocationForRequestStreamHandler(blockingHttpClient, nextInvocationHttpRequest);
                        } else if (handler instanceof RequestHandler<?, ?>) {
                            handleInvocationForRequestHandler(blockingHttpClient, nextInvocationHttpRequest);
                        }
                    }
                } finally {
                    if (handler instanceof Closeable closeable) {
                        closeable.close();
                    }
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            logn(LogLevel.ERROR, "Request loop failed with: ", e.getMessage());
            reportInitializationError(runtimeApiURL, e);
        }
    }

    /**
     * It handles an invocation event with a handler of type {@link RequestHandler}.
     * @param blockingHttpClient Blocking HTTP Client
     * @param nextInvocationHttpRequest Next Invocation HTTP Request
     * @throws IOException Exception thrown while invoking the handler
     */
    protected void handleInvocationForRequestHandler(@NonNull BlockingHttpClient blockingHttpClient,
                                                     @NonNull MutableHttpRequest<?> nextInvocationHttpRequest) throws IOException {
        final HttpResponse<RequestType> response = blockingHttpClient.exchange(nextInvocationHttpRequest, Argument.of(requestType));
        final RequestType request = response.body();
        if (request != null) {
            logn(LogLevel.DEBUG, "request body ", request);
            Context context = createRuntimeContext(response);
            final String requestId = context.getAwsRequestId();
            HandlerRequestType handlerRequest = createHandlerRequest(request);
            try {
                if (StringUtils.isNotEmpty(requestId)) {
                    log(LogLevel.TRACE, "invoking handler\n");
                    HandlerResponseType handlerResponse = null;
                    if (handler instanceof RequestHandler) {
                        handlerResponse = ((RequestHandler<HandlerRequestType, HandlerResponseType>) handler).handleRequest(handlerRequest, context);
                    }
                    log(LogLevel.TRACE, "handler response received\n");
                    final ResponseType functionResponse = (handlerResponse == null || handlerResponse instanceof Void) ? null : createResponse(handlerResponse);
                    log(LogLevel.TRACE, "sending function response\n");
                    blockingHttpClient.exchange(decorateWithUserAgent(invocationResponseRequest(requestId, functionResponse == null ? "" : functionResponse)));
                } else {
                    log(LogLevel.WARN, "request id is empty\n");
                }
            } catch (Exception e) {
                handleInvocationException(blockingHttpClient, requestId, e);
            }
        }
    }

    /**
     *
     * @param blockingHttpClient Blocking HTTP Client
     * @param requestId AWS Request ID retried via {@link Context#getAwsRequestId()}
     * @param exception Execption thrown invoking the handler
     */
    protected void handleInvocationException(@NonNull BlockingHttpClient blockingHttpClient,
                                             @NonNull String requestId,
                                             @NonNull Exception exception) {
        final StringWriter sw = new StringWriter();
        exception.printStackTrace(new PrintWriter(sw));
        logn(LogLevel.WARN, "Invocation with requestId [", requestId, "] failed: ", exception.getMessage(), sw);
        try {
            blockingHttpClient.exchange(decorateWithUserAgent(invocationErrorRequest(requestId, exception.getMessage(), null, null)));
        } catch (Exception e2) {
            // swallow, nothing we can do...
        }
    }

    /**
     *
     * @param response Next Invocation Response
     * @return a new {@link Context} backed by a {@link RuntimeContext} populated with the HTTP Headers of the Invocation Response.
     */
    protected Context createRuntimeContext(HttpResponse<?> response) {
        final HttpHeaders headers = response.getHeaders();
        propagateTraceId(headers);
        final Context context = new RuntimeContext(headers);
        final String requestId = context.getAwsRequestId();
        logn(LogLevel.DEBUG, "request id ", requestId, " found");
        return context;
    }

    /**
     * It handles an invocation event with a handler of type {@link RequestStreamHandler}.
     * @param blockingHttpClient Blocking HTTP Client
     * @param nextInvocationHttpRequest Next Invocation HTTP Request
     */
    protected void handleInvocationForRequestStreamHandler(@NonNull BlockingHttpClient blockingHttpClient,
                                                           MutableHttpRequest<?> nextInvocationHttpRequest) {
        if (handler instanceof RequestStreamHandler requestStreamHandler) {
            final HttpResponse<byte[]> response = blockingHttpClient.exchange(nextInvocationHttpRequest, byte[].class);
            final byte[] request = response.body();
            if (request != null) {
                Context context = createRuntimeContext(response);
                String requestId = context.getAwsRequestId();
                if (StringUtils.isNotEmpty(requestId)) {
                    try (InputStream inputStream = new ByteArrayInputStream(request)) {
                        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
                        log(LogLevel.TRACE, "invoking handler\n");
                        requestStreamHandler.handleRequest(inputStream, outputStream, context);
                        log(LogLevel.TRACE, "handler response received\n");
                        byte[] handlerResponse = outputStream.toByteArray();
                        log(LogLevel.TRACE, "sending function response\n");
                        blockingHttpClient.exchange(decorateWithUserAgent(invocationResponseRequest(requestId, handlerResponse)));
                    } catch (Exception e) {
                        handleInvocationException(blockingHttpClient, requestId, e);
                    }
                }
            } else {
                log(LogLevel.WARN, "request id is empty\n");
            }
        }
    }

    /**
     * If the request is {@link MutableHttpRequest} and {@link AbstractMicronautLambdaRuntime#userAgent} is not null,
     * it adds an HTTP Header User-Agent.
     * @param request HTTP Request
     * @return The HTTP Request decorated
     */
    protected HttpRequest decorateWithUserAgent(HttpRequest<?> request) {
        if (userAgent != null && request instanceof MutableHttpRequest<?> mutableHttpRequest) {
            return mutableHttpRequest.header(USER_AGENT, userAgent);
        }
        return request;
    }

    /**
     * Get the X-Ray tracing header from the Lambda-Runtime-Trace-Id header in the API response.
     * Set the _X_AMZN_TRACE_ID environment variable with the same value for the X-Ray SDK to use.
     * @param headers next API Response HTTP Headers
     */
    @SuppressWarnings("EmptyBlock")
    protected void propagateTraceId(HttpHeaders headers) {
        String traceId = headers.get(LambdaRuntimeInvocationResponseHeaders.LAMBDA_RUNTIME_TRACE_ID);
        logn(LogLevel.DEBUG, "Trace id: ", traceId, '\n');
        if (StringUtils.isNotEmpty(traceId)) {
            System.setProperty(XRayUtils.LAMBDA_TRACE_HEADER_PROP, traceId);
        }
    }

    /**
     * Reports Initialization error to the Runtime API.
     * @param runtimeApiURL Runtime API URL
     * @param e Exception thrown
     */
    protected void reportInitializationError(URL runtimeApiURL, Throwable e) {
        try (HttpClient endpointClient = HttpClient.create(runtimeApiURL)) {
            endpointClient.toBlocking().exchange(decorateWithUserAgent(initializationErrorRequest(e.getMessage(), null, null)));
        } catch (Throwable e2) {
            // swallow, nothing we can do...
        }
    }

    /**
     *
     * @param value Object to be serialized
     * @return A JSON String of the supplied object
     */
    @Nullable
    protected byte[] serializeAsByteArray(Object value) throws IOException {
        if (value == null) {
            return null;
        }
        ApplicationContext applicationContext = getApplicationContext();
        if (applicationContext != null && applicationContext.containsBean(JsonMapper.class)) {
            JsonMapper jsonMapper = applicationContext.getBean(JsonMapper.class);
            return jsonMapper.writeValueAsBytes(value);
        }
        return null;
    }

    /**
     *
     * @param content JSON String
     * @param valueType Class Type to be read into
     * @param <T> Type to be read into
     * @return a new Class build from the JSON String
     * @throws IOException if underlying input contains invalid content
     *   expected for result type (or has other mismatch issues)
     */
    @Nullable
    protected <T> T valueFromContent(String content, Class<T> valueType) throws IOException {
        if (content == null) {
            return null;
        }
        ApplicationContext applicationContext = getApplicationContext();
        if (applicationContext != null && applicationContext.containsBean(JsonMapper.class)) {
            JsonMapper objectMapper = applicationContext.getBean(JsonMapper.class);
            return objectMapper.readValue(content, Argument.of(valueType));
        }
        return null;
    }

    /**
     * @param name the name of the environment variable
     * @return the string value of the variable, or {@code null} if the variable is not defined
     */
    protected String getEnv(String name) {
        return System.getenv(name);
    }

    /**
     * @param level Log Level
     * @param msg Message to log
     */
    protected void log(LogLevel level, String msg) {
        if (shouldLog(level)) {
            LambdaRuntime.getLogger().log(msg);
        }
    }

    /**
     *
     * @return {@link LogLevel} for the custom runtime.
     */
    protected LogLevel getLogLevel() {
        return LogLevel.WARN;
    }

    /**
     * Log with a line break.
     * @param logLevel Log level
     * @param msg Message to log
     */
    protected void logn(LogLevel logLevel, String msg) {
        logn(logLevel, msg, '\n');
    }

    /**
     * @param level Log Level
     * @param messageParts One or more message parts to concatenate and log if enabled
     */
    protected void logn(LogLevel level, Object... messageParts) {
        if (!shouldLog(level)) {
            return;
        }

        StringBuilder sb = new StringBuilder();
        for (Object part : messageParts) {
            sb.append(part);
        }
        sb.append('\n');
        LambdaRuntime.getLogger().log(sb.toString());
    }

    /**
     *
     * @param level Log level
     * @return where it should log or not
     */
    protected boolean shouldLog(LogLevel level) {
        if (level == LogLevel.ALL) {
            return true;
        }
        if (level == LogLevel.OFF || level == LogLevel.NOT_SPECIFIED) {
            return false;
        }
        return getLogLevel().ordinal() <= level.ordinal();
    }

    private URL lookupRuntimeApiEndpoint() throws MalformedURLException {
        final String runtimeApiEndpoint = getEnv(ReservedRuntimeEnvironmentVariables.AWS_LAMBDA_RUNTIME_API);
        if (StringUtils.isEmpty(runtimeApiEndpoint)) {
            throw new IllegalStateException("Missing " + ReservedRuntimeEnvironmentVariables.AWS_LAMBDA_RUNTIME_API + " environment variable. Custom runtime can only be run within AWS Lambda environment.");
        }
        return new URL("http://" + runtimeApiEndpoint);
    }

    @SuppressWarnings("rawtypes")
    private Class initTypeArgument(int index) {
        final Class[] args = GenericTypeUtils.resolveSuperTypeGenericArguments(
            getClass(),
            AbstractMicronautLambdaRuntime.class
        );
        if (ArrayUtils.isNotEmpty(args) && args.length > index) {
            return args[index];
        } else {
            return Object.class;
        }
    }
}
