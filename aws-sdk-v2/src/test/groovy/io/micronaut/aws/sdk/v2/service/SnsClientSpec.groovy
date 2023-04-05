package io.micronaut.aws.sdk.v2.service

import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.SnsClient

class SnsClientSpec extends ServiceClientSpec<SnsClient, SnsAsyncClient> {
    @Override
    protected String serviceName() {
        return SnsClient.SERVICE_NAME
    }

    @Override
    protected SnsClient getClient() {
        applicationContext.getBean(SnsClient)
    }

    protected SnsAsyncClient getAsyncClient() {
        applicationContext.getBean(SnsAsyncClient)
    }
}
