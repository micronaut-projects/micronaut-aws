package io.micronaut.aws.distributedconfiguration

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.inject.BeanDefinition
import spock.lang.Specification

class AwsDistributedConfigurationPropertiesSpec extends Specification {

    void "AwsDistributedConfigurationProperties is annotated with BootstrapContextCompatible"() {
        given:
        ApplicationContext context = ApplicationContext.run()

        when:
        BeanDefinition<AwsDistributedConfigurationProperties> beanDefinition = context.getBeanDefinition(AwsDistributedConfigurationProperties)

        then:
        beanDefinition.getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()

        cleanup:
        context.close()
    }
}
