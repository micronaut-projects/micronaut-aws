package io.micronaut.aws.sdk.v2.client.netty

import io.micronaut.aws.sdk.v2.client.urlConnection.BootstrapUrlConnectionClientConfigurationProperties
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import spock.lang.Specification

class BootstrapNettyClientConfigurationPropertiesSpec extends Specification {
    void "By default BootstrapNettyClientConfigurationProperties is disabled"() {
        given:
        ApplicationContext context = ApplicationContext.run()

        expect:
        !context.containsBean(BootstrapNettyClientConfigurationProperties)
        context.containsBean(NettyClientConfigurationProperties)
        context.containsBean(NettyClientConfiguration)

        and:
        context.getBean(NettyClientConfiguration) instanceof NettyClientConfigurationProperties

        cleanup:
        context.close()
    }

    void "aws.sdk-http-client.bootstrap to true enables BootstrappacheClientFactory"() {
        given:
        ApplicationContext context = ApplicationContext.run([
                'aws.sdk-http-client.bootstrap': true
        ])

        expect:
        context.containsBean(BootstrapNettyClientConfigurationProperties)
        !context.containsBean(NettyClientConfigurationProperties)
        context.containsBean(NettyClientConfiguration)

        and:
        context.getBean(NettyClientConfiguration) instanceof BootstrapNettyClientConfigurationProperties


        and: 'BootstrapUrlConnectionClientConfigurationProperties is annotated with BootstrapContextCompatible'
        context.getBeanDefinition(BootstrapUrlConnectionClientConfigurationProperties).getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()

        cleanup:
        context.close()
    }
}
