package io.micronaut.function.aws.proxy.payload1

import io.micronaut.function.aws.proxy.SingletonBean
import spock.lang.Specification

class Payload1EagerlyInitializeSingletonTest extends Specification {

    void "verify singletons are eagerly initialized for Payload v1 function"() {
        given:
        ApiGatewayProxyRequestEventFunction handler = new ApiGatewayProxyRequestEventFunction()

        expect:
        SingletonBean.PACKAGE[ApiGatewayProxyRequestEventFunction.packageName]

        cleanup:
        handler.close()
    }
}
