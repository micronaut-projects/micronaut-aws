package io.micronaut.aws.secretsmanager

class SecretsManagerConfigurationSpec  extends ApplicationContextSpecification {

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                'aws.secretsmanager.enabled': false
        ]
    }

    void "you can disable secrets manager integration with aws.secretsmanager.enabled = false"() {
        expect:
        !applicationContext.containsBean(SecretsManagerConfiguration)
    }
}
