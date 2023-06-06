package io.micronaut.aws.secretsmanager

import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import io.micronaut.inject.BeanDefinition
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import jakarta.inject.Singleton

class SecretsManagerConfigurationClientSpec extends ApplicationContextSpecification {

    @Override
    String getSpecName() {
        'SecretsManagerConfigurationClientSpec'
    }

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                'aws.secretsmanager.enabled': true,
                'aws.secretsmanager.secrets': [
                        ["secret-name": "rds_default", "prefix": "datasources.default"],
                        ["secret-name": "rds_backup", "prefix": "datasources.backup"]
                ]
        ]
    }

    void "SecretsManagerConfigurationClient is annotated with BootstrapContextCompatible"() {
        when:
        BeanDefinition<SecretsManagerConfigurationClient> beanDefinition = applicationContext.getBeanDefinition(SecretsManagerConfigurationClient)

        then:
        beanDefinition.getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()
    }

    void "The adaptPropertyKey method call when secret manager configuration is provided"() {
        when:
        SecretsManagerConfigurationClient bean = applicationContext.getBean(SecretsManagerConfigurationClient)
        String adaptedPropertyKey = bean.adaptPropertyKey('host', 'rds_default')

        then:
        adaptedPropertyKey == 'datasources.default.host'
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
