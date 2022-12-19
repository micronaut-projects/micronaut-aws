package io.micronaut.aws.sdk.v2.service

import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient

class SecretsManagerClientSpec extends ServiceClientSpec<SecretsManagerClient, SecretsManagerAsyncClient> {
    @Override
    protected String serviceName() {
        return SecretsManagerClient.SERVICE_NAME
    }

    @Override
    protected SecretsManagerClient getClient() {
        applicationContext.getBean(SecretsManagerClient)
    }

    protected SecretsManagerAsyncClient getAsyncClient() {
        applicationContext.getBean(SecretsManagerAsyncClient)
    }
}
