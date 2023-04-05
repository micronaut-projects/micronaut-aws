package io.micronaut.aws.sdk.v2.service

import software.amazon.awssdk.services.ses.SesAsyncClient
import software.amazon.awssdk.services.ses.SesClient

class SesClientSpec extends ServiceClientSpec<SesClient, SesAsyncClient> {
    @Override
    protected String serviceName() {
        return SesClient.SERVICE_NAME
    }

    @Override
    protected SesClient getClient() {
        applicationContext.getBean(SesClient)
    }

    protected SesAsyncClient getAsyncClient() {
        applicationContext.getBean(SesAsyncClient)
    }
}
