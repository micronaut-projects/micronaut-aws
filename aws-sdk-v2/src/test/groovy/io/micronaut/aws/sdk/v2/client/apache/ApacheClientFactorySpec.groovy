package io.micronaut.aws.sdk.v2.client.apache

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import spock.lang.Specification

class ApacheClientFactorySpec extends Specification {

    void 'ApacheClientFactory is annotated with BootstrapContextCompatible'() {
        given:
        ApplicationContext context = ApplicationContext.run()

        expect:
        context.getBeanDefinition(ApacheClientFactory).getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()

        cleanup:
        context.close()
    }
}
