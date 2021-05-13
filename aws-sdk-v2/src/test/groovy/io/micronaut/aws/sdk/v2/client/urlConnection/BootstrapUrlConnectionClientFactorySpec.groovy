package io.micronaut.aws.sdk.v2.client.urlConnection

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import spock.lang.Specification

class BootstrapUrlConnectionClientFactorySpec extends Specification {

    void "By default BootstrapUrlConnectionClientFactory is disabled"() {
        given:
        ApplicationContext context = ApplicationContext.run()

        expect:
        !context.containsBean(BootstrapUrlConnectionClientFactory)
        context.containsBean(UrlConnectionClientFactory)

        cleanup:
        context.close()
    }

    void "aws.sdk-http-client.bootstrap to true enables BootstrapUrlConnectionClientFactory"() {
        given:
        ApplicationContext context = ApplicationContext.run([
                'aws.sdk-http-client.bootstrap': true
        ])

        expect:
        context.containsBean(BootstrapUrlConnectionClientFactory)
        !context.containsBean(UrlConnectionClientFactory)

        and: 'BootstrapUrlConnectionClientFactory is annoated with'
        context.getBeanDefinition(BootstrapUrlConnectionClientFactory).getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()

        cleanup:
        context.close()
    }
}
