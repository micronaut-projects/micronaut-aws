package io.micronaut.aws.sdk.v2.service

import software.amazon.awssdk.services.ssm.SsmAsyncClient
import software.amazon.awssdk.services.ssm.SsmClient

class SsmClientSpec extends ServiceClientSpec<SsmClient, SsmAsyncClient> {
    @Override
    protected String serviceName() {
        return SsmClient.SERVICE_NAME
    }

    @Override
    protected SsmClient getClient() {
        applicationContext.getBean(SsmClient)
    }

    protected SsmAsyncClient getAsyncClient() {
        applicationContext.getBean(SsmAsyncClient )
    }
}
