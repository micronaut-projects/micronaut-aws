package io.micronaut.aws.sdk.v2.client.netty

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

    void 'NettyClientConfiguration is not annotated with BootstrapContextCompatible'() {
        given:
        NettyClientConfiguration nettyClientConfiguration = new NettyClientConfiguration()

        expect:
        !nettyClientConfiguration.isProxyConfigured()

        when:
        nettyClientConfiguration.proxy.host('micronaut.example')

        then:
        nettyClientConfiguration.isProxyConfigured()
    }
}
