package io.micronaut.aws.sdk.v2.service

import software.amazon.awssdk.core.client.config.SdkClientOption
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient

class SecretsManagerClientSpec extends ServiceClientSpec {

    @Override
    protected String serviceName() {
        return SecretsManagerClient.SERVICE_NAME
    }

    void "it can configure a sync secrets manager client"() {
        when:
        SecretsManagerClient client = applicationContext.getBean(SecretsManagerClient)

        then:
        client.serviceName() == serviceName()
    }

    void "it can configure an async secrets manager client"() {
        when:
        SecretsManagerAsyncClient client = applicationContext.getBean(SecretsManagerAsyncClient)

        then:
        client.serviceName() == serviceName()
    }

    void "it can override the endpoint"() {
        when:
        SecretsManagerClient client = applicationContext.getBean(SecretsManagerClient)
        SecretsManagerAsyncClient asyncClient = applicationContext.getBean(SecretsManagerAsyncClient)

        then:
        client.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
        asyncClient.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
    }
}
