package io.micronaut.function.aws.proxy.payload2

import io.micronaut.function.aws.proxy.Clock
import io.micronaut.function.aws.proxy.payload1.ApiGatewayProxyRequestEventFunction
import spock.lang.Specification

import java.time.LocalDateTime

class PayloadV2EagerlyInitializeSingletonTest extends Specification {

    void "verify singletons are eagerly initialized for Payload v2 function"() {
        given:
        APIGatewayV2HTTPEventFunction handler = new APIGatewayV2HTTPEventFunction()
        sleep(5_000)

        expect:
        handler.getApplicationContext().getBean(Clock).now.isBefore(LocalDateTime.now().minusSeconds(3))

        cleanup:
        handler.close()
    }
}
