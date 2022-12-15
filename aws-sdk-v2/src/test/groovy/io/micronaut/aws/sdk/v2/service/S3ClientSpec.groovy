package io.micronaut.aws.sdk.v2.service

import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Client

class S3ClientSpec extends ServiceClientSpec<S3Client, S3AsyncClient> {
    @Override
    protected String serviceName() {
        return S3Client.SERVICE_NAME
    }

    @Override
    protected S3Client getClient() {
        applicationContext.getBean(S3Client)
    }

    protected S3AsyncClient getAsyncClient() {
        applicationContext.getBean(S3AsyncClient)
    }
}
