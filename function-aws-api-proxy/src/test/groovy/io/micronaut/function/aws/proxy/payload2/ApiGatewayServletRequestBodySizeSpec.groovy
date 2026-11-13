package io.micronaut.function.aws.proxy.payload2

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import io.micronaut.core.convert.ConversionService
import io.micronaut.http.HttpHeaders
import io.micronaut.http.MediaType
import io.micronaut.http.body.stream.BodySizeLimits
import io.micronaut.http.exceptions.ContentLengthExceededException
import spock.lang.Specification

class ApiGatewayServletRequestBodySizeSpec extends Specification {

    void "the content length is the declared header when present"() {
        expect:
        request("hello", [(HttpHeaders.CONTENT_LENGTH): "42"]).contentLength == 42L
    }

    void "the content length falls back to the event body length when no header is declared"() {
        expect:
        request("hello", [:]).contentLength == 5L
    }

    void "the content length is zero when the event has no body"() {
        expect:
        request(null, [:]).contentLength == 0L
    }

    void "a body within the limit is readable"() {
        given:
        def servletRequest = request("hello", [:])
        servletRequest.setBodySizeLimits(new BodySizeLimits(5, 5))

        expect:
        servletRequest.inputStream.text == "hello"
        servletRequest.byteBody().expectedLength().asLong == 5L
    }

    void "reading a body over the limit fails with ContentLengthExceededException"() {
        given:
        def servletRequest = request("hello", [:])
        servletRequest.setBodySizeLimits(new BodySizeLimits(4, 4))

        when:
        read.call(servletRequest)

        then:
        thrown(ContentLengthExceededException)

        where:
        read << [
            { APIGatewayV2HTTPEventServletRequest r -> r.inputStream },
            { APIGatewayV2HTTPEventServletRequest r -> r.byteBody() },
            { APIGatewayV2HTTPEventServletRequest r -> r.getParametersFromBody([:]) },
        ]
    }

    void "form parameters within the limit are parsed from the body"() {
        given:
        def servletRequest = request("a=1&b=2", [(HttpHeaders.CONTENT_TYPE): MediaType.APPLICATION_FORM_URLENCODED])
        servletRequest.setBodySizeLimits(new BodySizeLimits(100, 100))

        expect:
        servletRequest.getParametersFromBody([:]).get("b") == "2"
    }

    private static APIGatewayV2HTTPEventServletRequest request(String body, Map<String, String> headers) {
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent()
        event.setHeaders(headers)
        event.setBody(body)
        event.setRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder()
            .withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder().withMethod("POST").withPath("/").build())
            .build())
        new APIGatewayV2HTTPEventServletRequest(event, null, ConversionService.SHARED, null)
    }
}
