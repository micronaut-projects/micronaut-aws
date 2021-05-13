package io.micronaut.aws.sdk.v2.client.netty

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import spock.lang.Specification

class BootstrapNettyClientFactorySpec extends Specification {

    void "By default BootstrapNettyClientFactory is disabled"() {
        given:
        ApplicationContext context = ApplicationContext.run()

        expect:
        !context.containsBean(BootstrapNettyClientFactory)
        context.containsBean(NettyClientFactory)

        cleanup:
        context.close()
    }

    void "aws.sdk-http-client.bootstrap to true enables BootstrapNettyClientFactory"() {
        given:
        ApplicationContext context = ApplicationContext.run([
                'aws.sdk-http-client.bootstrap': true
        ])

        expect:
        context.containsBean(BootstrapNettyClientFactory)
        !context.containsBean(NettyClientFactory)

        and: 'BootstrapNettyClientFactory is annotated with BootstrapContextCompatible'
        context.getBeanDefinition(BootstrapNettyClientFactory).getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()

        cleanup:
        context.close()
    }
}
