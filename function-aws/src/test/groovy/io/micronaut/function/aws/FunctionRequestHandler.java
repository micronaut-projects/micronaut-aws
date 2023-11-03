package io.micronaut.function.aws;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.json.JsonMapper;
import jakarta.inject.Inject;

import java.io.IOException;
import java.util.Collections;

public class FunctionRequestHandler extends MicronautRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    @Inject
    JsonMapper objectMapper;

    @Override
    public APIGatewayProxyResponseEvent execute(APIGatewayProxyRequestEvent input) {
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent();
        try {
            String json = new String(objectMapper.writeValueAsBytes(Collections.singletonMap("message", "Hello World")));
            response.setStatusCode(200);
            response.setBody(json);
        } catch (IOException e) {
            response.setStatusCode(500);
        }
        return response;
    }
}

