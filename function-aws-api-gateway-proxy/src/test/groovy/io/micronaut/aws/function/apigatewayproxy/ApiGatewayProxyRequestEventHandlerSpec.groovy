package io.micronaut.aws.function.apigatewayproxy

import io.micronaut.context.ApplicationContextProvider
import spock.lang.Specification

class ApiGatewayProxyRequestEventHandlerSpec extends Specification {

    void "ApiGatewayProxyRequestEventHandler is an ApplicationContextProvider"() {
        given:
        ApiGatewayProxyRequestEventHandler handler = new ApiGatewayProxyRequestEventHandler()

        expect:
        handler instanceof ApplicationContextProvider
    }
}
