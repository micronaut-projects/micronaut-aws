package io.micronaut.http.server.tck.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.aws.function.apigatewayproxy.ApiGatewayProxyRequestEventHandler;
import io.micronaut.aws.function.apigatewayproxy.ApiGatewayProxyResponseEventAdapter;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.core.type.Argument;
import io.micronaut.function.aws.LambdaApplicationContextBuilder;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.tck.ServerUnderTest;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class LambdaServerUnderTest implements ServerUnderTest {
    private ApiGatewayProxyRequestEventHandler handler;
    private Context lambdaContext;

    public LambdaServerUnderTest(Map<String, Object> properties) {
        ApplicationContextBuilder contextBuilder = new LambdaApplicationContextBuilder();
        contextBuilder.properties(properties);
        this.handler = new ApiGatewayProxyRequestEventHandler(contextBuilder);
        this.lambdaContext = new MockLambdaContext();

    }

    @Override
    public <I, O> HttpResponse<O> exchange(HttpRequest<I> request, Argument<O> bodyType) {
        APIGatewayProxyRequestEvent input = adaptRequest(request);
        APIGatewayProxyResponseEvent awsProxyResponse = handler.handleRequest(input, lambdaContext);
        return adaptReponse(awsProxyResponse);
    }

    @Override
    public <I, O, E> HttpResponse<O> exchange(HttpRequest<I> request, Argument<O> bodyType, Argument<E> errorType) {
        return exchange(request, bodyType);
    }

    private <I> APIGatewayProxyRequestEvent adaptRequest(HttpRequest<I> request) {
        APIGatewayProxyRequestEvent requestEvent = new APIGatewayProxyRequestEvent();
        requestEvent.setPath(request.getPath());
        requestEvent.setHttpMethod(request.getMethod().toString());

        Map<String, List<String>> multivalueHeaders = request.getHeaders().asMap();
        Map<String, String> headers = new HashMap<>();
        for (String headerName : multivalueHeaders.keySet()) {
            headers.put(headerName, String.join(",", multivalueHeaders.get(headerName)));
        }
        requestEvent.setHeaders(headers);
        requestEvent.setMultiValueHeaders(multivalueHeaders);
        //TODO the rest
        return requestEvent;
    }

    private <O> HttpResponse<O> adaptReponse(APIGatewayProxyResponseEvent awsProxyResponse) {
        MutableHttpResponse<O> response = new ApiGatewayProxyResponseEventAdapter<>(awsProxyResponse, getApplicationContext().getConversionService());
        if (response.getStatus().getCode() >= 400) {
            throw new HttpClientResponseException("error", response);
        }
        response.body(awsProxyResponse.getBody());
        return response;
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return handler.getApplicationContext();
    }

    @Override
    public Optional<Integer> getPort() {
        // Need a port for the CORS tests
        return Optional.of(1234);
    }

    @Override
    public void close() throws IOException {
        this.handler.close();
    }
}
