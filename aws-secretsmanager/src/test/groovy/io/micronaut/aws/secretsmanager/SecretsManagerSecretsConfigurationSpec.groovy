package io.micronaut.aws.secretsmanager

class SecretsManagerSecretsConfigurationSpec extends ApplicationContextSpecification  {

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                'aws.secretsmanager.enabled': true,
                'aws.secretsmanager.secrets': [
                        ["secretName": "rds_default", "prefix": "datasources.default"],
                        ["secretName": "rds_backup", "prefix": "datasources.backup"]
                ]
        ]
    }

    void "secrets manager loads secrets configuration"() {
        def secretsManagerConfiguration = applicationContext.getBean(SecretsManagerConfiguration)

        expect:
        !secretsManagerConfiguration.getSecrets().isEmpty()

        when:
        List secretHolder = secretsManagerConfiguration.getSecrets()
        SecretConfiguration rdsDefaultSecret = secretHolder[0]
        SecretConfiguration rdsBackupSecret = secretHolder[1]

        then:
        rdsDefaultSecret.secretName == 'rds_default'
        rdsDefaultSecret.prefix == 'datasources.default'
        rdsBackupSecret.secretName == 'rds_backup'
        rdsBackupSecret.prefix == 'datasources.backup'
    }
}
