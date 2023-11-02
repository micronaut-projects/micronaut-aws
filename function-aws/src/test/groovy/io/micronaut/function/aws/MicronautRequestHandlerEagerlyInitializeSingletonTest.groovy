package io.micronaut.function.aws

import spock.lang.Specification

import java.time.LocalDateTime

class MicronautRequestHandlerEagerlyInitializeSingletonTest extends Specification {

    void "verify singletons are eagerly initialized for alb function"() {
        given:
        FunctionRequestHandler handler = new FunctionRequestHandler()
        sleep(5_000)

        expect:
        handler.getApplicationContext().getBean(Clock).now.isBefore(LocalDateTime.now().minusSeconds(3))

        cleanup:
        handler.close()
    }
}
