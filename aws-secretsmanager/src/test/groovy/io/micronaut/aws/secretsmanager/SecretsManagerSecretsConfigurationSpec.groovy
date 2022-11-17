package io.micronaut.aws.secretsmanager

class SecretsManagerSecretsConfigurationSpec extends ApplicationContextSpecification  {

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

    void "secrets manager loads secrets configuration"() {
        def secretsManagerConfiguration = applicationContext.getBean(SecretsManagerConfiguration)

        expect:
        !secretsManagerConfiguration.getSecrets().isEmpty()

        when:
        List secretConfigurations = secretsManagerConfiguration.getSecrets()

        then:
        secretConfigurations.size() == 2
        for (secretConfiguration in secretConfigurations) {
            if (secretConfiguration.getSecretName() == 'rds_default') {
                secretConfiguration.getPrefix() == 'datasources.default'
            } else if (secretConfiguration.getSecretName() == 'rds_backup') {
                secretConfiguration.getPrefix() == 'datasources.backup'
            }
        }
    }
}
