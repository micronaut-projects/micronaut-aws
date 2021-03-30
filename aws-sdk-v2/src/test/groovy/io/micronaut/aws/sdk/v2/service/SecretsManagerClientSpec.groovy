package io.micronaut.aws.sdk.v2.service;

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient

class SecretsManagerClientSpec extends ApplicationContextSpecification {

    void "it can configure a sync secrets manager client"() {
        when:
        SecretsManagerClient client = applicationContext.getBean(SecretsManagerClient)

        then:
        client.serviceName() == SecretsManagerClient.SERVICE_NAME
    }

    void "it can configure an async secrets manager client"() {
        when:
        SecretsManagerAsyncClient client = applicationContext.getBean(SecretsManagerAsyncClient)

        then:
        client.serviceName() == SecretsManagerClient.SERVICE_NAME
    }
}
