package io.micronaut.aws.secretsmanager

class SecretsManagerSecretsConfigurationSpec extends ApplicationContextSpecification  {

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                'aws.secretsmanager.enabled': true,
                'aws.secretsmanager.secrets': [
                        ["secret": "rds_default", "prefix": "datasources.default"],
                        ["secret": "rds_backup", "prefix": "datasources.backup"]
                ]
        ]
    }

    void "secrets manager loads secrets configuration"() {
        def secretsManagerConfiguration = applicationContext.getBean(SecretsManagerConfiguration)

        expect:
        !secretsManagerConfiguration.getSecrets().isEmpty()

        when:
        List secretHolder = secretsManagerConfiguration.getSecrets()
        SecretHolder rdsDefaultSecret = secretHolder[0]
        SecretHolder rdsBackupSecret = secretHolder[1]

        then:
        rdsDefaultSecret.secret == 'rds_default'
        rdsDefaultSecret.prefix == 'datasources.default'
        rdsBackupSecret.secret == 'rds_backup'
        rdsBackupSecret.prefix == 'datasources.backup'
    }
}
