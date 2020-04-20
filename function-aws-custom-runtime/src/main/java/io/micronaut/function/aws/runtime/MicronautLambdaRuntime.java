/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
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
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextProvider;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.reflect.ClassUtils;
import io.micronaut.core.reflect.GenericTypeUtils;
import io.micronaut.core.util.ArrayUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.DefaultHttpClientConfiguration;
import io.micronaut.http.client.RxHttpClient;

import javax.annotation.Nonnull;
import java.io.Closeable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Predicate;

/**
 * Class that can be used as a entry point for a custom Lambda runtime.
 *
 * @param <RequestType> The expected request object. This is the model class that the event JSON is de-serialized to
 * @param <ResponseType> The expected Lambda function response object. Responses will be written to this model object
 * @param <HandlerRequestType> The request type for {@link com.amazonaws.services.lambda.runtime.RequestHandler}.
 * @param <HandlerResponseType> The response type for the {@link com.amazonaws.services.lambda.runtime.RequestHandler}.
 *
 * @author graemerocher
 * @since 1.1
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

//                com.amazonaws.services.lambda.runtime.events.DynamodbEvent.class,
//
//                com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsFirehoseInputPreprocessingEvent.class,
//                com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsInputPreprocessingResponse.class,
//                com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsOutputDeliveryEvent.class,
//                com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsOutputDeliveryResponse.class,
//                com.amazonaws.services.lambda.runtime.events.KinesisAnalyticsStreamsInputPreprocessingEvent.class,
//                com.amazonaws.services.lambda.runtime.events.KinesisEvent.class,
//                com.amazonaws.services.lambda.runtime.events.KinesisFirehoseEvent.class,

                com.amazonaws.services.lambda.runtime.events.LexEvent.class,

//Exception in thread "main" java.lang.ArrayStoreException: sun.reflect.annotation.TypeNotPresentExceptionProxy
//                com.amazonaws.services.lambda.runtime.events.S3Event.class,

                com.amazonaws.services.lambda.runtime.events.SNSEvent.class,
                com.amazonaws.services.lambda.runtime.events.SQSEvent.class
        }
)
public abstract class MicronautLambdaRuntime<RequestType, ResponseType, HandlerRequestType, HandlerResponseType> implements ApplicationContextProvider, AwsLambdaRuntimeApi {

    protected MicronautRequestHandler<HandlerRequestType, HandlerResponseType> handler;

    @SuppressWarnings("unchecked")
    protected final Class<RequestType> requestType = initTypeArgument(0);

    @SuppressWarnings("unchecked")
    protected final Class<ResponseType> responseType = initTypeArgument(1);

    @SuppressWarnings("unchecked")
    protected final Class<HandlerRequestType> handlerRequestType = initTypeArgument(2);

    @SuppressWarnings("unchecked")
    protected final Class<HandlerResponseType> handlerResponseType = initTypeArgument(3);

    private Class initTypeArgument(int index) {
        final Class[] args = GenericTypeUtils.resolveSuperTypeGenericArguments(
                getClass(),
                MicronautLambdaRuntime.class
        );
        if (ArrayUtils.isNotEmpty(args) && args.length > index) {
            return args[index];
        } else {
            return Object.class;
        }
    }

    public MicronautLambdaRuntime() {
    }

    /**
     * Starts the runtime API event loop.
     * @throws MalformedURLException if the lambda endpoint URL is malformed
     **/
    public void run(String... args) throws MalformedURLException {
        handler = createHandler(args);
        final URL runtimeApiURL = lookupRuntimeApiEndpoint();
        logn("runtime endpoint: " + runtimeApiURL.toString());
        final Predicate<URL> loopUntil = (url) -> true;
        startRuntimeApiEventLoop(runtimeApiURL, loopUntil);
    }

    @Override
    public ApplicationContext getApplicationContext() {
        if (handler instanceof ApplicationContextProvider) {
            return ((ApplicationContextProvider) handler).getApplicationContext();
        }
        return null;
    }

    protected MicronautRequestHandler<HandlerRequestType, HandlerResponseType> createHandler(String... args) {
        String handler = getEnv(ReservedRuntimeEnvironmentVariables.HANDLER);
        logn("Handler: " + handler);
        if (handler != null) {
            Optional<Class> handlerClassOptional = parseHandlerClass(handler);
            if (handlerClassOptional.isPresent()) {
                Class handlerClass = handlerClassOptional.get();
                final BeanIntrospection introspection = BeanIntrospection.getIntrospection(handlerClass);
                return (MicronautRequestHandler<HandlerRequestType, HandlerResponseType>) introspection.instantiate();
            }
        }
        return null;
    }

    protected Optional<Class> parseHandlerClass(@NonNull String handler) {
        String[] arr = handler.split("::");
        if (arr.length > 0) {
            return ClassUtils.forName(arr[0], null);
        }
        return Optional.empty();
    }

    protected ResponseType createResponse(HandlerResponseType handlerResponse) throws Exception {
        if (handlerResponseType == responseType) {
            logn("HandlerResponseType and ResponseType are identical");
            return (ResponseType) handlerResponse;

        } else if (responseType == APIGatewayProxyResponseEvent.class) {
            logn("response type is APIGatewayProxyResponseEvent");
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

    protected APIGatewayProxyResponseEvent respond(HttpStatus status, String body, String contentType) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.CONTENT_TYPE, contentType);
        response.setHeaders(headers);
        response.setBody(body);
        response.setStatusCode(status.getCode());
        logn("response: " + status.getCode() + " content type: " + headers.get(HttpHeaders.CONTENT_TYPE) + " message " + body);
        return response;
    }


    protected HandlerRequestType createHandlerRequest(RequestType request) throws Exception {
        if (requestType == handlerRequestType) {
            return (HandlerRequestType) request;
        } else if (request instanceof APIGatewayProxyRequestEvent) {
            logn("request of type APIGatewayProxyRequestEvent");
            String content = ((APIGatewayProxyRequestEvent) request).getBody();
            return valueFromContent(content, handlerRequestType);
        }
        logn("createHandlerRequest return null");
        return null;
    }



    /**
     * Starts the runtime API event loop.
     *
     * @param runtimeApiURL             The runtime API URL.
     * @param loopUntil                 A predicate that allows controlling when the event loop should exit
     */
    protected void startRuntimeApiEventLoop(@Nonnull URL runtimeApiURL,
                                            @Nonnull Predicate<URL> loopUntil) {
        try {
            ApplicationContext applicationContext = getApplicationContext();
            if (applicationContext == null) {
                throw new ConfigurationException("Application Context is null");
            }
            final DefaultHttpClientConfiguration config = new DefaultHttpClientConfiguration();
            config.setReadIdleTimeout(null);
            config.setReadTimeout(null);
            config.setConnectTimeout(null);
            final RxHttpClient endpointClient = applicationContext.createBean(
                    RxHttpClient.class,
                    runtimeApiURL,
                    config);
            try {
                while (loopUntil.test(runtimeApiURL)) {
                    final BlockingHttpClient blockingHttpClient = endpointClient.toBlocking();
                    final HttpResponse<RequestType> response = blockingHttpClient.exchange(AwsLambdaRuntimeApi.NEXT_INVOCATION_URI, requestType);
                    final RequestType request = response.body();
                    if (request != null) {
                        logn("request body " + request.toString());

                        HandlerRequestType handlerRequest = createHandlerRequest(request);
                        final HttpHeaders headers = response.getHeaders();
                        propagateTraceId(headers);

                        final Context context = new RuntimeContext(headers);
                        final String requestId = context.getAwsRequestId();
                        logn("request id " + requestId + " found");
                        try {
                            if (StringUtils.isNotEmpty(requestId)) {
                                logn("invoking handler");

                                final HandlerResponseType handlerResponse = handler.handleRequest(handlerRequest, context);
                                logn("handler response received");
                                final ResponseType functionResponse = (handlerResponse == null || handlerResponse instanceof Void) ? null : createResponse(handlerResponse);
                                logn("sending function response");
                                endpointClient.exchange(invocationResponseRequest(requestId, functionResponse == null ? "" : functionResponse)).blockingSubscribe();

                            } else {
                                logn("request id is empty");
                            }

                        } catch (Throwable e) {
                            final StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            logn("Invocation with requestId [" + requestId + "] failed: " + e.getMessage() + sw.toString());
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
            logn("Request loop failed with: " + e.getMessage());
            reportInitializationError(runtimeApiURL, e);
        }
    }

    /**
     * Get the X-Ray tracing header from the Lambda-Runtime-Trace-Id header in the API response.
     * Set the _X_AMZN_TRACE_ID environment variable with the same value for the X-Ray SDK to use.
     * @param headers next API Response HTTP Headers
     */
    public void propagateTraceId(HttpHeaders headers) {
        String traceId = headers.get(LambdaRuntimeInvocationResponseHeaders.LAMBDA_RUNTIME_TRACE_ID);
        logn("Trace id: " + traceId + "\n");
        if (StringUtils.isNotEmpty(traceId)) {
            //TODO Set Env.variable _X_AMZN_TRACE_ID with value traceId
        }
    }

    void reportInitializationError(URL runtimeApiURL, Throwable e) {
        try (RxHttpClient endpointClient = RxHttpClient.create(runtimeApiURL)) {
            endpointClient.toBlocking().exchange(initializationErrorRequest(e.getMessage(), null, null));
        } catch (Throwable e2) {
            // swallow, nothing we can do...
        }
    }

    @Nullable
    protected String serializeAsJsonString(Object value) throws Exception {
        if (value == null) {
            return null;
        }
        ApplicationContext applicationContext = getApplicationContext();
        if (applicationContext != null) {
            if (applicationContext.containsBean(ObjectMapper.class)) {
                ObjectMapper objectMapper = applicationContext.getBean(ObjectMapper.class);
                try {
                    return objectMapper.writeValueAsString(value);
                } catch (JsonProcessingException e) {
                    return null;
                }
            }
        }
        return null;
    }

    @Nullable
    protected <T> T valueFromContent(String content, Class<T> valueType) throws Exception {
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

    private URL lookupRuntimeApiEndpoint() throws MalformedURLException {
        final String runtimeApiEndpoint = getEnv(ReservedRuntimeEnvironmentVariables.AWS_LAMBDA_RUNTIME_API);
        if (StringUtils.isEmpty(runtimeApiEndpoint)) {
            throw new IllegalStateException("Missing " + ReservedRuntimeEnvironmentVariables.AWS_LAMBDA_RUNTIME_API + " environment variable. Custom runtime can only be run within AWS Lambda environment.");
        }
        return new URL("http://" + runtimeApiEndpoint);
    }

    /**
     * @param name the name of the environment variable
     * @return the string value of the variable, or {@code null} if the variable is not defined
     */
    protected String getEnv(String name) {
        return System.getenv(name);
    }

    protected void log(String msg) {
        LambdaRuntime.getLogger().log(msg);
    }

    protected void logn(String msg) {
        log(msg + "\n");
    }
}
