package io.micronaut.function.aws.proxy.payload2

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import io.micronaut.http.HttpHeaders
import spock.lang.Specification

class APIGatewayV2HTTPEventServletRequestSpec extends Specification {

    void "headers with multivalue for v2"() {
        given:
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent()
        Map<String, String> headers = new HashMap<>()
        headers.put(HttpHeaders.DATE, "Wed, 21 Oct 2015 07:28:00 GMT")
        headers.put("foo", "Bar")
        headers.put("key", "value1,value2,value3")
        headers.put(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko)")
        event.setHeaders(headers)
        event.setRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder().withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder().withMethod("GET").build()).build())

        when:
        APIGatewayV2HTTPEventServletRequest servletRequest = new APIGatewayV2HTTPEventServletRequest(event, null, null, null)

        then:
        servletRequest
        "Bar" == servletRequest.getHeaders().get("Foo")
        ["Bar"] == servletRequest.getHeaders().getAll("Foo")
        ["value1,value2,value3"] == servletRequest.getHeaders().getAll("Key")
    }
}
