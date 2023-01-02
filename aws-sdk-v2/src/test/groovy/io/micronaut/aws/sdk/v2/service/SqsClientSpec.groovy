package io.micronaut.aws.sdk.v2.service

import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.SqsClient

class SqsClientSpec extends ServiceClientSpec<SqsClient, SqsAsyncClient> {
    @Override
    protected String serviceName() {
        return SqsClient.SERVICE_NAME
    }

    @Override
    protected SqsClient getClient() {
        applicationContext.getBean(SqsClient)
    }

    protected SqsAsyncClient getAsyncClient() {
        applicationContext.getBean(SqsAsyncClient)
    }
}
