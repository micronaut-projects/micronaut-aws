/*
 * Copyright 2017-2022 original authors
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
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.cli.CommandLine;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.core.type.Argument;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.aws.MicronautLambdaContext;
import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.DefaultHttpClientConfiguration;
import io.micronaut.http.client.HttpClient;
import io.micronaut.context.env.CommandLinePropertySource;
import io.micronaut.logging.LogLevel;
import io.micronaut.serde.ObjectMapper;
import io.micronaut.serde.annotation.SerdeImport;

import java.io.Closeable;
import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

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
        accessType = {TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS, TypeHint.AccessType.ALL_PUBLIC},
        value = {
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
@SerdeImport(com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.class)
@SerdeImport(com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext.class)
@SerdeImport(com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.RequestIdentity.class)
@SerdeImport(com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent.class)
@SerdeImport(com.amazonaws.services.lambda.runtime.events.ScheduledEvent.class)
@SerdeImport(com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyRequestEvent.class)
@SerdeImport(com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent.class)
@SerdeImport(com.amazonaws.services.lambda.runtime.events.CloudFrontEvent.class)
@SerdeImport(com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent.class)
@SerdeImport(com.amazonaws.services.lambda.runtime.events.CodeCommitEvent.class)
@SerdeImport(com.amazonaws.services.lambda.runtime.events.CognitoEvent.class)
@SerdeImport(com.amazonaws.services.lambda.runtime.events.ConfigEvent.class)
@SerdeImport(com.amazonaws.services.lambda.runtime.events.IoTButtonEvent.class)
@SerdeImport(com.amazonaws.services.lambda.runtime.events.LexEvent.class)
@SerdeImport(com.amazonaws.services.lambda.runtime.events.SNSEvent.class)
@SerdeImport(com.amazonaws.services.lambda.runtime.events.SQSEvent.class)
public abstract class AbstractMicronautLambdaRuntime<RequestType, ResponseType, HandlerRequestType, HandlerResponseType>
        implements ApplicationContextProvider, AwsLambdaRuntimeApi {

    static final String USER_AGENT_VALUE = String.format(
            "micronaut/%s-%s",
            System.getProperty("java.vendor.version"),
            AbstractMicronautLambdaRuntime.class.getPackage().getImplementationVersion());

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
        final Predicate<URL> loopUntil = (url) -> true;
        startRuntimeApiEventLoop(runtimeApiURL, loopUntil, args);
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
        if (handler instanceof ApplicationContextProvider) {
            return ((ApplicationContextProvider) handler).getApplicationContext();
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
        String handler = getEnv(ReservedRuntimeEnvironmentVariables.HANDLER);
        logn(LogLevel.DEBUG, "Handler: ", handler);
        if (handler != null) {
            Optional<Class> handlerClassOptional = parseHandlerClass(handler);
            logn(LogLevel.WARN, "No handler Class parsed for ", handler);
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
    protected Optional<Class> parseHandlerClass(@NonNull String handler) {
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

        } else if (responseType == APIGatewayProxyResponseEvent.class) {
            log(LogLevel.TRACE, "response type is APIGatewayProxyResponseEvent\n");
            String json = serializeAsJsonString(handlerResponse);
            if (json != null) {
                return (ResponseType) respond(HttpStatus.OK, json, MediaType.APPLICATION_JSON);
            }
            return (ResponseType) respond(HttpStatus.BAD_REQUEST,
                    "Could not serialize " + handlerResponse.toString() + " as json",
                    MediaType.TEXT_PLAIN);
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
    protected APIGatewayProxyResponseEvent respond(HttpStatus status, String body, String contentType) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, contentType);
        response.setHeaders(headers);
        response.setBody(body);
        response.setStatusCode(status.getCode());
        logn(LogLevel.TRACE, "response: ", status.getCode(), " content type: ", headers.get(HttpHeaders.CONTENT_TYPE), " message ", body);
        return response;
    }


    /**
     *
     * @param request Request obtained from the Runtime API
     * @return if the request and the handler type are the same, just return the request, if the request is of type {@link APIGatewayProxyRequestEvent} it attempts to build an object of type HandlerRequestType with the body of the request, else returns {@code null}
     * @throws IOException if underlying request body contains invalid content or the request body JSON structure does not match structure
     *   expected for result type (or has other mismatch issues)
     */
    @Nullable
    protected HandlerRequestType createHandlerRequest(RequestType request) throws IOException {
        if (requestType == handlerRequestType) {
            return (HandlerRequestType) request;
        } else if (request instanceof APIGatewayProxyRequestEvent) {
            log(LogLevel.TRACE, "request of type APIGatewayProxyRequestEvent\n");
            String content = ((APIGatewayProxyRequestEvent) request).getBody();
            return valueFromContent(content, handlerRequestType);
        }
        log(LogLevel.TRACE, "createHandlerRequest return null\n");
        return null;
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
            final DefaultHttpClientConfiguration config = new DefaultHttpClientConfiguration();
            config.setReadIdleTimeout(null);
            config.setReadTimeout(null);
            config.setConnectTimeout(null);
            final HttpClient endpointClient = applicationContext.createBean(
                    HttpClient.class,
                    runtimeApiURL,
                    config);
            final BlockingHttpClient blockingHttpClient = endpointClient.toBlocking();
            try {
                while (loopUntil.test(runtimeApiURL)) {
                    final HttpResponse<RequestType> response = blockingHttpClient.exchange(
                            HttpRequest.GET(AwsLambdaRuntimeApi.NEXT_INVOCATION_URI).header(USER_AGENT, USER_AGENT_VALUE), Argument.of(requestType));
                    final RequestType request = response.body();
                    if (request != null) {
                        logn(LogLevel.DEBUG, "request body ", request);

                        HandlerRequestType handlerRequest = createHandlerRequest(request);
                        final HttpHeaders headers = response.getHeaders();
                        propagateTraceId(headers);

                        final Context context = new RuntimeContext(headers);
                        final String requestId = context.getAwsRequestId();
                        logn(LogLevel.DEBUG, "request id ", requestId, " found");
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
                                blockingHttpClient.exchange(invocationResponseRequest(requestId, functionResponse == null ? "" : functionResponse));
                            } else {
                                log(LogLevel.WARN, "request id is empty\n");
                            }

                        } catch (Throwable e) {
                            final StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            logn(LogLevel.WARN, "Invocation with requestId [", requestId, "] failed: ", e.getMessage(), sw);
                            try {
                                blockingHttpClient.exchange(invocationErrorRequest(requestId, e.getMessage(), null, null));

                            } catch (Throwable e2) {
                                // swallow, nothing we can do...
                            }
                        }
                    }
                }
            } finally {
                if (handler instanceof Closeable) {
                    ((Closeable) handler).close();
                }
                if (endpointClient != null) {
                    endpointClient.close();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            logn(LogLevel.ERROR, "Request loop failed with: ", e.getMessage());
            reportInitializationError(runtimeApiURL, e);
        }
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
            System.setProperty(MicronautRequestHandler.LAMBDA_TRACE_HEADER_PROP, traceId);
        }
    }

    /**
     * Reports Initialization error to the Runtime API.
     * @param runtimeApiURL Runtime API URL
     * @param e Exception thrown
     */
    protected void reportInitializationError(URL runtimeApiURL, Throwable e) {
        try (HttpClient endpointClient = HttpClient.create(runtimeApiURL)) {
            endpointClient.toBlocking().exchange(initializationErrorRequest(e.getMessage(), null, null));
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
    protected String serializeAsJsonString(Object value) {
        if (value == null) {
            return null;
        }
        ApplicationContext applicationContext = getApplicationContext();
        if (applicationContext != null) {
            if (applicationContext.containsBean(ObjectMapper.class)) {
                ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper.class);
                try {
                    return objectMapper.writeValueAsString(value);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return null;
    }

    /**
     *
     * @param content JSON String
     * @param valueType Class Type to be read into
     * @param <T> Type to be read into
     * @return a new Class build from the JSON String
     * @throws IOException if underlying input contains invalid content, or does not match structure
     *   expected for result type (or has other mismatch issues)
     */
    @Nullable
    protected <T> T valueFromContent(String content, Class<T> valueType) throws IOException {
        if (content == null) {
            return null;
        }
        ApplicationContext applicationContext = getApplicationContext();
        if (applicationContext != null) {
            if (applicationContext.containsBean(ObjectMapper.class)) {
                ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper.class);

                return objectMapper.readValue(content, valueType);
            }
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
