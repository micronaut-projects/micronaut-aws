package io.micronaut.aws.secretsmanager
import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.inject.BeanDefinition

class SecretsManagerConfigurationPropertiesSpec extends ApplicationContextSpecification {

    void "SecretsManagerConfigurationProperties is annotated with BootstrapContextCompatible"() {
        when:
        BeanDefinition<SecretsManagerConfigurationProperties> beanDefinition = applicationContext.getBeanDefinition(SecretsManagerConfigurationProperties)

        then:
        beanDefinition.getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()
    }
}
