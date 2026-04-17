package io.micronaut.aws.secretsmanager

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.BootstrapContextCompatible
import io.micronaut.context.env.Environment
import io.micronaut.inject.BeanDefinition
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import jakarta.inject.Singleton
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires

class SecretsManagerLegacyCharacterizationSpec extends ApplicationContextSpecification {

    @Override
    String getSpecName() {
        'SecretsManagerLegacyCharacterizationSpec'
    }

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
            'aws.secretsmanager.enabled': true,
            'aws.secretsmanager.secrets': [
                ['secret-name': 'rds_default', 'prefix': 'datasources.default'],
                ['secret-name': 'rds_backup', 'prefix': 'datasources.backup']
            ]
        ]
    }

    void "legacy secrets manager configuration client is bootstrap compatible and enabled by default"() {
        when:
        BeanDefinition<SecretsManagerConfigurationClient> beanDefinition = applicationContext.getBeanDefinition(SecretsManagerConfigurationClient)
        SecretsManagerConfiguration configuration = applicationContext.getBean(SecretsManagerConfiguration)
        SecretsManagerConfigurationClient client = applicationContext.getBean(SecretsManagerConfigurationClient)

        then:
        beanDefinition.getAnnotationNameByStereotype(BootstrapContextCompatible).isPresent()
        configuration.enabled
        client.description == 'AWS Secrets Manager'
    }

    void "legacy secrets manager adapts configured grouped secret prefixes"() {
        given:
        SecretsManagerConfigurationClient client = applicationContext.getBean(SecretsManagerConfigurationClient)

        expect:
        client.adaptPropertyKey('host', 'rds_default') == 'datasources.default.host'
        client.adaptPropertyKey('username', 'rds_backup') == 'datasources.backup.username'
        client.adaptPropertyKey('url', 'unmapped_secret') == 'url'
    }

    void "legacy secrets manager integration can be disabled entirely"() {
        when:
        ApplicationContext disabledContext = ApplicationContext.run([
            'spec.name': 'SecretsManagerLegacyCharacterizationSpec-disabled',
            'aws.secretsmanager.enabled': false
        ])

        then:
        !disabledContext.containsBean(SecretsManagerConfiguration)
        !disabledContext.containsBean(SecretsManagerConfigurationClient)

        cleanup:
        disabledContext.close()
    }

    @Requires(property = 'spec.name', value = 'SecretsManagerLegacyCharacterizationSpec')
    @Primary
    @Singleton
    static class MockSecretsClient implements SecretsManagerClient {
        @Override
        String serviceName() {
            'secrets manager'
        }

        @Override
        void close() {
        }
    }
}
