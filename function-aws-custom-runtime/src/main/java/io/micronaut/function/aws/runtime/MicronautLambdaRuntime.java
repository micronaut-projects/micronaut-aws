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

import static io.micronaut.http.HttpHeaders.USER_AGENT;

import java.io.Closeable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;
import java.util.function.Predicate;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaRuntime;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
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
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.aws.MicronautLambdaContext;
import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.function.aws.XRayUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.DefaultHttpClientConfiguration;
import io.micronaut.http.client.HttpClient;
import io.micronaut.logging.LogLevel;

/**
 * Class that can be used as a entry point for a AWS Lambda custom runtime.
 * @see <a href="https://docs.aws.amazon.com/lambda/latest/dg/runtimes-custom.html">Custom AWS Lambda runtimes</a>.
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

                com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.class,
                com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse.class,

                com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerRequestEvent.class,
                com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerResponseEvent.class,

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
public class MicronautLambdaRuntime implements ApplicationContextProvider, AwsLambdaRuntimeApi {
    @Nullable
    protected String userAgent;

    protected Object handler;

    /**
     * Constructor.
     */
    public MicronautLambdaRuntime() {
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
     * Uses {@link UserAgentProvider} to populate {@link MicronautLambdaRuntime#userAgent}.
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
        if (!(handler instanceof MicronautRequestHandler || handler instanceof RequestStreamHandler)) {
            throw new ConfigurationException("handler must be of type io.micronaut.function.aws.MicronautRequestHandler or com.amazonaws.services.lambda.runtime.RequestStreamHandler");
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
    protected RequestHandler<?, ?> createRequestHandler(String... args) {
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
     * @return if {@link MicronautLambdaRuntime#createHandler(String...)} or {@link MicronautLambdaRuntime#createRequestStreamHandler(String...)} are implemented, it returns the handler returned by those methods. If they are not, it attempts to instantiate the class
     * referenced by the environment variable {@link ReservedRuntimeEnvironmentVariables#HANDLER} via Introspection. Otherwise, it returns {@code null}.
     */
    @Nullable
    protected Object createHandler(String... args) {
        RequestHandler<?, ?> requestHandler = createRequestHandler(args);
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
            // TODO: Clean up this part
            MicronautRequestHandler<?, ?> requestHandler = (MicronautRequestHandler<?, ?>) handler;
            ApplicationContext applicationContext = getApplicationContext();
            if (applicationContext == null) {
                throw new ConfigurationException("Application Context is null");
            }
            populateUserAgent();
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
                    MutableHttpRequest<?> nextInvocationHttpRequest = HttpRequest.GET(AwsLambdaRuntimeApi.NEXT_INVOCATION_URI);
                    applicationContext.findBean(UserAgentProvider.class)
                        .ifPresent(userAgentProvider -> nextInvocationHttpRequest.header(USER_AGENT, userAgentProvider.userAgent()));
                    final HttpResponse<?> response = blockingHttpClient.exchange(nextInvocationHttpRequest, requestHandler.inputTypeClass());
                    final Object request = response.body();
                    if (request != null) {
                        logn(LogLevel.DEBUG, "request body ", request);

                        final HttpHeaders headers = response.getHeaders();
                        propagateTraceId(headers);

                        final Context context = new RuntimeContext(headers);
                        final String requestId = context.getAwsRequestId();
                        logn(LogLevel.DEBUG, "request id ", requestId, " found");
                        try {
                            if (StringUtils.isNotEmpty(requestId)) {
                                log(LogLevel.TRACE, "invoking handler\n");
                                Object handlerResponse = null;
                                if (handler instanceof MicronautRequestHandler) {
                                    handlerResponse = ((MicronautRequestHandler<Object, ?>) handler).handleRequest(request, context);
                                }
                                log(LogLevel.TRACE, "handler response received\n");
                                log(LogLevel.TRACE, "sending function response\n");
                                blockingHttpClient.exchange(decorateWithUserAgent(invocationResponseRequest(requestId, handlerResponse == null ? "" : handlerResponse)));
                            } else {
                                log(LogLevel.WARN, "request id is empty\n");
                            }

                        } catch (Throwable e) {
                            final StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            logn(LogLevel.WARN, "Invocation with requestId [", requestId, "] failed: ", e.getMessage(), sw);
                            try {
                                blockingHttpClient.exchange(decorateWithUserAgent(invocationErrorRequest(requestId, e.getMessage(), null, null)));
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
     * If the request is {@link MutableHttpRequest} and {@link MicronautLambdaRuntime#userAgent} is not null,
     * it adds an HTTP Header User-Agent.
     * @param request HTTP Request
     * @return The HTTP Request decorated
     */
    protected HttpRequest decorateWithUserAgent(HttpRequest<?> request) {
        if (userAgent != null && request instanceof MutableHttpRequest) {
            return ((MutableHttpRequest) request).header(USER_AGENT, userAgent);
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

    public static void main(String[] args) {
        try {
            new MicronautLambdaRuntime().run(args);
        } catch (Exception e) {
            throw new ConfigurationException("Exception thrown instantiating MicronautLambdaRuntime", e);
        }
    }
}
