package io.micronaut.aws.sdk.v2.client.urlConnection

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import spock.lang.Specification

class BootstrapUrlConnectionClientConfigurationPropertiesSpec extends Specification {
    void "By default BootstrapNettyClientFactory is disabled"() {
        given:
        ApplicationContext context = ApplicationContext.run()

        expect:
        !context.containsBean(BootstrapUrlConnectionClientConfigurationProperties)
        context.containsBean(UrlConnectionClientConfigurationProperties)
        context.containsBean(UrlConnectionClientConfiguration)

        and:
        context.getBean(UrlConnectionClientConfiguration) instanceof UrlConnectionClientConfigurationProperties

        cleanup:
        context.close()
    }

    void "aws.sdk-http-client.bootstrap to true enables BootstrapNettyClientFactory"() {
        given:
        ApplicationContext context = ApplicationContext.run([
                'aws.sdk-http-client.bootstrap': true
        ])

        expect:
        context.containsBean(BootstrapUrlConnectionClientConfigurationProperties)
        !context.containsBean(UrlConnectionClientConfigurationProperties)
        context.containsBean(UrlConnectionClientConfiguration)

        and:
        context.getBean(UrlConnectionClientConfiguration) instanceof BootstrapUrlConnectionClientConfigurationProperties


        and: 'BootstrapUrlConnectionClientConfigurationProperties is annotated with BootstrapContextCompatible'
        context.getBeanDefinition(BootstrapUrlConnectionClientConfigurationProperties).getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()

        cleanup:
        context.close()
    }

}
