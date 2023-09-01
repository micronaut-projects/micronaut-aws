package io.micronaut.function.aws.proxy.payload1

import io.micronaut.function.aws.proxy.ApiGatewayServletRequest
import spock.lang.Specification

class ApiGatewayProxyServletRequestSpec extends Specification {


    void "if path is null method does not throw NPE"() {
        when:
        ApiGatewayServletRequest.buildUri(null, [:], [:])

        then:
        noExceptionThrown()
    }
}
