package io.micronaut.aws.secretsmanager

import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import io.micronaut.inject.BeanDefinition
import jakarta.inject.Singleton
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient

class SecretsManagerConfigurationClientSpec extends ApplicationContextSpecification {

    @Override
    String getSpecName() {
        'SecretsManagerConfigurationClientSpec'
    }

    void "SecretsManagerConfigurationClient is annotated with BootstrapContextCompatible"() {
        when:
        BeanDefinition<SecretsManagerConfigurationClient> beanDefinition = applicationContext.getBeanDefinition(SecretsManagerConfigurationClient)

        then:
        beanDefinition.getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()
    }

    @Requires(property = 'spec.name', value = 'SecretsManagerConfigurationClientSpec')
    @Primary
    @Singleton
    static class MockSecretsClient implements SecretsManagerClient {

        @Override
        String serviceName() {
            return "secrets manager"
        }

        @Override
        void close() {

        }
    }
}
