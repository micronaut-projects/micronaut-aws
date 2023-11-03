package io.micronaut.function.aws.proxy.alb

import io.micronaut.function.aws.proxy.SingletonBean
import spock.lang.Specification

class AlbEagerlyInitializeSingletonTest extends Specification {

    void "verify singletons are eagerly initialized for alb function"() {
        given:
        ApplicationLoadBalancerFunction handler = new ApplicationLoadBalancerFunction()

        expect:
        SingletonBean.PACKAGE == ApplicationLoadBalancerFunction.packageName

        cleanup:
        handler.close()
    }
}
