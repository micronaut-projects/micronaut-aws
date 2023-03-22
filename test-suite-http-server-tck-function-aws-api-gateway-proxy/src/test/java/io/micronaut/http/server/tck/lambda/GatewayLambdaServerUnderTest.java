package io.micronaut.http.server.tck.lambda;

import com.amazonaws.services.lambda.runtime.Context;
import io.micronaut.aws.function.apigatewayproxy.ApiGatewayProxyEventFunction;
import io.micronaut.aws.function.apigatewayproxy.ApiGatewayProxyResponseEventAdapter;
import io.micronaut.aws.function.apigatewayproxy.ApiGatewayProxyServletResponse;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.convert.value.MutableConvertibleValues;
import io.micronaut.core.type.Argument;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.exceptions.HttpClientResponseException;
import io.micronaut.http.tck.ServerUnderTest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

public class GatewayLambdaServerUnderTest implements ServerUnderTest {

    private static final Logger LOG = LoggerFactory.getLogger(GatewayLambdaServerUnderTest.class);

    private ApiGatewayProxyEventFunction function;
    private Context lambdaContext;

    public GatewayLambdaServerUnderTest(Map<String, Object> properties) {
        properties.put("micronaut.server.context-path", "/");
        this.function = new ApiGatewayProxyEventFunction(ApplicationContext
            .builder(Environment.FUNCTION, Environment.GOOGLE_COMPUTE, Environment.TEST)
            .properties(properties)
            .deduceEnvironment(false)
            .start());
    }

    @Override
    public <I, O> HttpResponse<O> exchange(HttpRequest<I> request, Argument<O> bodyType) {
        HttpResponse<O> response = new ApiGatewayProxyResponseEventAdapter<>(function.handleRequest(request).getNativeResponse(), getApplicationContext().getConversionService());
        if (LOG.isDebugEnabled()) {
            LOG.debug("Response status: {}", response.getStatus());
        }
        if (response.getStatus().getCode() >= 400) {
            throw new HttpClientResponseException("error " + response.getStatus().getReason() + " (" + response.getStatus().getCode() + ")", response);
        }
        return response;
    }

    @Override
    public <I, O, E> HttpResponse<O> exchange(HttpRequest<I> request, Argument<O> bodyType, Argument<E> errorType) {
        return exchange(request, bodyType);
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return function.getApplicationContext();
    }

    @Override
    public Optional<Integer> getPort() {
        return Optional.of(1234);
    }

    @Override
    public void close() throws IOException {
        function.close();
    }
}
