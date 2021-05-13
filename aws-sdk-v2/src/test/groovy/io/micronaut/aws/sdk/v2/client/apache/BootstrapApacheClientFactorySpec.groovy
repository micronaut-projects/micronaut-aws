package io.micronaut.aws.sdk.v2.client.apache

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import spock.lang.Specification

class BootstrapApacheClientFactorySpec extends Specification {

    void "By default BootstrapApacheClientFactory is disabled"() {
        given:
        ApplicationContext context = ApplicationContext.run()

        expect:
        !context.containsBean(BootstrapApacheClientFactory)
        context.containsBean(ApacheClientFactory)

        cleanup:
        context.close()
    }

    void "aws.sdk-http-client.bootstrap to true enables BootstrapApacheClientFactory"() {
        given:
        ApplicationContext context = ApplicationContext.run([
                'aws.sdk-http-client.bootstrap': true
        ])

        expect:
        context.containsBean(BootstrapApacheClientFactory)
        !context.containsBean(ApacheClientFactory)

        and: 'BootstrapApacheClientFactory is annoated with'
        context.getBeanDefinition(BootstrapApacheClientFactory).getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()

        cleanup:
        context.close()
    }
}
