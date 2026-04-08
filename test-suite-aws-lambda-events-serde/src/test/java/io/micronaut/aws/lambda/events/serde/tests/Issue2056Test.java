package io.micronaut.aws.lambda.events.serde.tests;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.function.aws.proxy.MockLambdaContext;
import io.micronaut.function.aws.proxy.payload1.ApiGatewayProxyRequestEventFunction;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpStatus;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

class Issue2056Test {

    private ApiGatewayProxyRequestEventFunction handler;

    @BeforeEach
    void setupSpec() {
        handler = new ApiGatewayProxyRequestEventFunction(
            ApplicationContext.builder()
                .properties(Map.of("micronaut.propagation", "thread-local"))
                .build()
        );
    }

    @AfterEach
    void cleanupSpec() throws Exception {
        handler.close();
    }

    @Test
    void testNotFoundEncoding() {
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent();
        request.setPath("/not_a_valid_url");
        request.setHttpMethod(HttpMethod.GET.toString());
        var response = handler.handleRequest(request, new MockLambdaContext());

        assertEquals(HttpStatus.NOT_FOUND.getCode(), response.getStatusCode());
    }
}
