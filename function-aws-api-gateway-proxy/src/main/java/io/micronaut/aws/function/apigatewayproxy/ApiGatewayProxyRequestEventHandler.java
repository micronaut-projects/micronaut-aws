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
package io.micronaut.aws.function.apigatewayproxy;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.execution.ExecutionFlow;
import io.micronaut.core.execution.ImperativeExecutionFlow;
import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.server.RouteExecutor;
import jakarta.inject.Inject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * {@link com.amazonaws.services.lambda.runtime.RequestHandler(APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent) which starts a Micronaut Application Context in the constructor and handles the full route processing lifecycle for a request.
 * @author Sergio del Amo
 * @since 4.0.0
 *
 */
public class ApiGatewayProxyRequestEventHandler extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Inject
    RouteExecutor routeExecutor;

    @Inject
    ConversionService conversionService;

    /**
     * Constructor.
     */
    public ApiGatewayProxyRequestEventHandler() {
    }

    /**
     * Constructor.
     * @param applicationContext Application Context
     */
    public ApiGatewayProxyRequestEventHandler(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    /**
     * Constructor.
     * @param applicationContextBuilder ApplicationContextBuilder
     */
    public ApiGatewayProxyRequestEventHandler(ApplicationContextBuilder applicationContextBuilder) {
        super(applicationContextBuilder);
    }

    @Override
    public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent input) {
        HttpRequest<?> request = new ApiGatewayProxyRequestEventAdapter<>(conversionService, input);
        HttpResponse<?> response = handle(request);
        if (response.getStatus().getCode() >= 400) {
            throw new HttpClientResponseException("error", response);
        }
        return adaptResponse(request, response);
    }

    private HttpResponse<?> handle(HttpRequest<?> request) {
        AmazonApiGatewayRequestLifecycle requestLifecycle = new AmazonApiGatewayRequestLifecycle(routeExecutor, request);
        ExecutionFlow<MutableHttpResponse<?>> flow = requestLifecycle.run();
        ImperativeExecutionFlow<MutableHttpResponse<?>> mutableHttpResponseImperativeExecutionFlow = flow.tryComplete();
        return mutableHttpResponseImperativeExecutionFlow.getValue();
    }

    private APIGatewayProxyResponseEvent adaptResponse(HttpRequest<?> request, HttpResponse<?> response) {
        //TODO Extract this adaptation to a  class where we can reuse it in the TCK logic
        APIGatewayProxyResponseEvent responseEvent = new APIGatewayProxyResponseEvent();
        response.getBody(String.class).ifPresent(responseEvent::setBody);
        responseEvent.setStatusCode(response.code());

        Map<String, List<String>> multiValueHeaders = response.getHeaders().asMap();
        responseEvent.setMultiValueHeaders(multiValueHeaders);
        Map<String, String> headers = new HashMap<>();
        for (String headerName : multiValueHeaders.keySet()) {
            headers.put(headerName, String.join(",", multiValueHeaders.get(headerName)));
        }
        responseEvent.setHeaders(headers);

        return responseEvent;
    }
}
