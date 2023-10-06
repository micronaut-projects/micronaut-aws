package io.micronaut.function.aws.proxy.payload1

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import io.micronaut.function.aws.proxy.ApiGatewayServletRequest
import spock.lang.Specification

class ApiGatewayProxyServletRequestSpec extends Specification {


    void "if path is null method does not throw NPE"() {
        when:
        ApiGatewayServletRequest.buildUri(null, [:], [:])

        then:
        noExceptionThrown()
    }

    void "headers with multivalue for v1"() {
        given:
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
        Map<String, String> headers = new HashMap<>()
        headers.put("Foo", "Bar")
        headers.put("Key", "value1,value2,value3")
        request.setHeaders(headers)
        Map<String, List<String>> multiValueHeaders = new HashMap<>()
        multiValueHeaders.put("Key", Arrays.asList("value1", "value2", "value3"))
        multiValueHeaders.put("Foo", Collections.singletonList("Bar"))
        request.setMultiValueHeaders(multiValueHeaders)
        request.setHttpMethod("GET")

        when:
        ApiGatewayProxyServletRequest servletRequest = new ApiGatewayProxyServletRequest(request, null, null, null)

        then:
        servletRequest
        "Bar" == servletRequest.getHeaders().get("Foo")
        ["Bar"] == servletRequest.getHeaders().getAll("Foo")
        ["value1", "value2", "value3"] == servletRequest.getHeaders().getAll("Key")
    }
}
