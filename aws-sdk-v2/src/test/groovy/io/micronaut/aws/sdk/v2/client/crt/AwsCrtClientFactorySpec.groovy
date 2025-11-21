package io.micronaut.aws.sdk.v2.client.crt

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import spock.lang.Specification

class AwsCrtClientFactorySpec extends Specification {

    void 'AwsCrtClientFactory is annotated with BootstrapContextCompatible'() {
        given:
        ApplicationContext context = ApplicationContext.run()

        expect:
        context.getBeanDefinition(AwsCrtClientFactory).getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()

        cleanup:
        context.close()
    }
}
