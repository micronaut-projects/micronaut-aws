package io.micronaut.aws.sdk.v2.service

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification
import software.amazon.awssdk.core.client.config.SdkClientOption
import software.amazon.awssdk.core.retry.RetryMode
import software.amazon.awssdk.services.s3.S3ClientBuilder

class S3ClientBuilderSpec extends ApplicationContextSpecification {

    @Override
    String getSpecName() {
        'S3ClientSpec.builders'
    }

    void "builders can be customised"() {
        when:
        S3ClientBuilder builder = applicationContext.getBean(S3ClientBuilder)

        then:
        noExceptionThrown()
        builder.syncClientConfiguration().option(SdkClientOption.RETRY_POLICY).retryMode() == RetryMode.LEGACY
    }
}
