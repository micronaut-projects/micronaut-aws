package io.micronaut.aws.sdk.v2

import io.micronaut.aws.sdk.v2.service.AWSServiceConfigurationProperties
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.inject.BeanDefinition
import spock.lang.Specification

class AWSServiceConfigurationPropertiesSpec extends Specification {
    void "AWSServiceConfigurationProperties is annotated with BootstrapContextCompatible"() {
        given:
        Map<String, String> properties = [
            "aws.services.ssm.endpoint-override": "http://localhost:4566"
        ]
        ApplicationContext applicationContext = ApplicationContext.run(properties)

        when:
        BeanDefinition<AWSServiceConfigurationProperties> beanDefinition = applicationContext.getBeanDefinition(AWSServiceConfigurationProperties)

        then:
        beanDefinition.getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()

        cleanup:
        applicationContext.close()
    }
}
