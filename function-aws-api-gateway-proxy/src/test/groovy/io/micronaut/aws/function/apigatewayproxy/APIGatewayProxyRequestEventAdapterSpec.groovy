package io.micronaut.aws.function.apigatewayproxy

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.fasterxml.jackson.databind.ObjectMapper
import io.micronaut.http.HttpRequest
import spock.lang.Specification

class APIGatewayProxyRequestEventAdapterSpec extends Specification {

    void "adapter works with v1"(String filename) {
        given:
        ApiGatewayProxyRequestEventHandler handler = new ApiGatewayProxyRequestEventHandler()
        InputStream inputStream = APIGatewayProxyRequestEventAdapterSpec.class.getResourceAsStream("/" + filename)

        when:
        ObjectMapper objectMapper = handler.getApplicationContext().getBean(ObjectMapper)

        then:
        objectMapper

        when:
        APIGatewayProxyRequestEvent request = objectMapper.readValue(inputStream, APIGatewayProxyRequestEvent)

        then:
        request

        when:
        HttpRequest<?> micronautRequest = new ApiGatewayProxyRequestEventAdapter<>(request)

        then:
        '/' == micronautRequest.path

        cleanup:
        handler.close()

        where:
        filename << ['apiGatewayProxyEventV1.json']
    }
}
