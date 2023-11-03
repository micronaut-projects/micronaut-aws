package io.micronaut.function.aws.proxy.payload2

import io.micronaut.function.aws.proxy.SingletonBean
import spock.lang.Specification

class PayloadV2EagerlyInitializeSingletonTest extends Specification {

    void "verify singletons are eagerly initialized for Payload v2 function"() {
        given:
        APIGatewayV2HTTPEventFunction handler = new APIGatewayV2HTTPEventFunction()

        expect:
        SingletonBean.PACKAGE == APIGatewayV2HTTPEventFunction.packageName

        cleanup:
        handler.close()
    }
}
