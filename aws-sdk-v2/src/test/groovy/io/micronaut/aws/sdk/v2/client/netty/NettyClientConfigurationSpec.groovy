package io.micronaut.aws.sdk.v2.client.netty

import io.micronaut.aws.sdk.v2.client.apache.ApacheClientFactory
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import spock.lang.Specification

class NettyClientConfigurationSpec extends Specification {

    void 'NettyClientConfiguration is annotated with BootstrapContextCompatible'() {
        given:
        ApplicationContext context = ApplicationContext.run()

        expect:
        context.getBeanDefinition(NettyClientConfiguration).getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()

        cleanup:
        context.close()
    }
}
