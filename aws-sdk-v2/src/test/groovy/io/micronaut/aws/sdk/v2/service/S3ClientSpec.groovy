package io.micronaut.aws.sdk.v2.service

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Client

class S3ClientSpec extends ApplicationContextSpecification {

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
}
