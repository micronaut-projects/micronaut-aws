package io.micronaut.aws.sdk.v2.client.urlConnection

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import spock.lang.Specification

class UrlConnectionClientConfigurationSpec extends Specification {

    void 'UrlConnectionClientConfiguration is annotated with BootstrapContextCompatible'() {
        given:
        ApplicationContext context = ApplicationContext.run()

        expect:
        context.getBeanDefinition(UrlConnectionClientConfiguration).getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()

        cleanup:
        context.close()
    }
}
