package io.micronaut.function.aws

import spock.lang.Specification

class MicronautRequestHandlerEagerlyInitializeSingletonTest extends Specification {

    void "verify singletons are eagerly initialized for alb function"() {
        given:
        FunctionRequestHandler handler = new FunctionRequestHandler()

        expect:
        SingletonBean.PACKAGE[FunctionRequestHandler.packageName]

        cleanup:
        handler.close()
    }
}
