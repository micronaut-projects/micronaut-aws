package io.micronaut.function.aws.proxy.payload1

import io.micronaut.function.aws.proxy.Clock
import spock.lang.Specification

import java.time.LocalDateTime

class Payload1EagerlyInitializeSingletonTest extends Specification {

    void "verify singletons are eagerly intialized for Payload v1 function"() {
        given:
        ApiGatewayProxyRequestEventFunction handler = new ApiGatewayProxyRequestEventFunction()
        sleep(5_000)

        expect:
        handler.getApplicationContext().getBean(Clock).now.isBefore(LocalDateTime.now().minusSeconds(3))

        cleanup:
        handler.close()
    }
}
