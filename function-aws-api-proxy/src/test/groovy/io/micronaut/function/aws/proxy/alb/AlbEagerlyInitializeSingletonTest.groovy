package io.micronaut.function.aws.proxy.alb

import io.micronaut.function.aws.proxy.Clock
import io.micronaut.function.aws.proxy.payload2.APIGatewayV2HTTPEventFunction
import spock.lang.Specification

import java.time.LocalDateTime

class AlbEagerlyInitializeSingletonTest extends Specification {

    void "verify singletons are eagerly initialized for alb function"() {
        given:
        ApplicationLoadBalancerFunction handler = new ApplicationLoadBalancerFunction()
        sleep(5_000)

        expect:
        handler.getApplicationContext().getBean(Clock).now.isBefore(LocalDateTime.now().minusSeconds(3))

        cleanup:
        handler.close()
    }
}
