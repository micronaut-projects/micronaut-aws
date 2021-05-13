package io.micronaut.aws.sdk.v2.client.apache

import io.micronaut.aws.sdk.v2.client.urlConnection.BootstrapUrlConnectionClientConfigurationProperties
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import spock.lang.Specification

class BootstrapApacheClientConfigurationPropertiesSpec extends Specification {
    void "By default BootstrapApacheClientConfigurationProperties is disabled"() {
        given:
        ApplicationContext context = ApplicationContext.run()

        expect:
        !context.containsBean(BootstrapApacheClientConfigurationProperties)
        context.containsBean(ApacheClientConfigurationProperties)
        context.containsBean(ApacheClientConfiguration)

        and:
        context.getBean(ApacheClientConfiguration) instanceof ApacheClientConfigurationProperties

        cleanup:
        context.close()
    }

    void "aws.sdk-http-client.bootstrap to true enables BootstrappacheClientFactory"() {
        given:
        ApplicationContext context = ApplicationContext.run([
                'aws.sdk-http-client.bootstrap': true
        ])

        expect:
        context.containsBean(BootstrapApacheClientConfigurationProperties)
        !context.containsBean(ApacheClientConfigurationProperties)
        context.containsBean(ApacheClientConfiguration)

        and:
        context.getBean(ApacheClientConfiguration) instanceof BootstrapApacheClientConfigurationProperties


        and: 'BootstrapUrlConnectionClientConfigurationProperties is annotated with BootstrapContextCompatible'
        context.getBeanDefinition(BootstrapUrlConnectionClientConfigurationProperties).getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()

        cleanup:
        context.close()
    }
}
