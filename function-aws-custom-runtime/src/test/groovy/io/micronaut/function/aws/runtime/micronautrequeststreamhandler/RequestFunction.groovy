package io.micronaut.function.aws.runtime.micronautrequeststreamhandler

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import io.micronaut.function.FunctionBean
import io.micronaut.json.JsonMapper
import jakarta.inject.Inject
import org.slf4j.Logger
import org.slf4j.LoggerFactory

import java.io.IOException
import java.util.Collections
import java.util.function.Function

@FunctionBean("requestfunction")
public class RequestFunction implements Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(RequestFunction.class)

    @Inject
    JsonMapper jsonMapper

    @Override
    public APIGatewayProxyResponseEvent apply(APIGatewayProxyRequestEvent requestEvent) {
        LOG.info("request {}", requestEvent)
        APIGatewayProxyResponseEvent response = new APIGatewayProxyResponseEvent()
        try {
            String json = new String(jsonMapper.writeValueAsBytes(Collections.singletonMap("message", "Hello World")))
            response.setStatusCode(200)
            response.setBody(json)
        } catch (IOException e) {
            response.setStatusCode(500)
        }
        LOG.info("response {}", response)
        response
    }
}
