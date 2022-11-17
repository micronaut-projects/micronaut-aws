package io.micronaut.aws.sdk.v2.service

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification
import software.amazon.awssdk.core.client.config.SdkClientOption
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3ClientBuilder

class S3ClientSpec extends ApplicationContextSpecification {

    private static final String ENDPOINT = "https://localhost:1234"

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                "aws.s3.endpoint-override": ENDPOINT
        ]
    }

    void "it can configure an S3 client"() {
        when:
        S3Client client = applicationContext.getBean(S3Client)

        then:
        client.serviceName() == S3Client.SERVICE_NAME
    }

    void "it can configure an S3 async client"() {
        when:
        S3AsyncClient client = applicationContext.getBean(S3AsyncClient)

        then:
        client.serviceName() == S3Client.SERVICE_NAME
    }

    void "it can override endpoint"() {
        when:
        S3ClientBuilder builder = applicationContext.getBean(S3ClientBuilder)

        then:
        builder.syncClientConfiguration().option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
    }
}
