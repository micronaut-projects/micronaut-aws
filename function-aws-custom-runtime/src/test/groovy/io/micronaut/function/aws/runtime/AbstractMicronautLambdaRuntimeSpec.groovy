package io.micronaut.function.aws.runtime

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent
import io.micronaut.context.exceptions.ConfigurationException
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import spock.lang.Specification


class AbstractMicronautLambdaRuntimeSpec extends Specification {

    void "validateHandler fails if handler is null"() {
        given:
        CustomRuntime customRuntime = new CustomRuntime()

        when:
        customRuntime.validateHandler()

        then:
        thrown(ConfigurationException)

        and:
        null == customRuntime.createRequestHandler()

        and:
        null == customRuntime.createRequestStreamHandler()

        when:
        APIGatewayProxyResponseEvent responseEvent = customRuntime.respond(HttpStatus.I_AM_A_TEAPOT, "{\"foo\":\"bar\"}".getBytes(), MediaType.APPLICATION_JSON_GITHUB)
        then:
        responseEvent.isBase64Encoded
        418 == responseEvent.statusCode
        MediaType.APPLICATION_JSON_GITHUB == responseEvent.getHeaders().get(HttpHeaders.CONTENT_TYPE)
        new String(Base64.encoder.encode("{\"foo\":\"bar\"}".getBytes())) == responseEvent.body
    }
    
    static class CustomRuntime extends AbstractMicronautLambdaRuntime<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent, APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    }
}



