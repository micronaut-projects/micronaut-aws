package io.micronaut.aws.sdk.v2.client.crt

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import spock.lang.Specification

class AwsCrtClientConfigurationSpec extends Specification {

    void 'AwsCrtClientConfiguration is annotated with BootstrapContextCompatible'() {
        given:
        ApplicationContext context = ApplicationContext.run()

        expect:
        context.getBeanDefinition(AwsCrtClientConfiguration).getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()

        cleanup:
        context.close()
    }
}
