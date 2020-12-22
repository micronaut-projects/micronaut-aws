package io.micronaut.aws.sdk.v2.service

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import software.amazon.awssdk.core.client.config.SdkClientOption
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Client
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest(rebuildContext = true)
class S3ClientSpec extends Specification {
    @Inject
    ApplicationContext applicationContext

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

    @Property(name = "aws.s3.endpoint-override", value = "https://test.io")
    void "it can have custom endpoint configurations"() {
        when:
        S3Client client = applicationContext.getBean(S3Client)
        S3AsyncClient asyncClient = applicationContext.getBean(S3AsyncClient)

        then:
        client.clientConfiguration.option(SdkClientOption.ENDPOINT) == URI.create("https//test.io")
        asyncClient.clientConfiguration.option(SdkClientOption.ENDPOINT) == URI.create("https//test.io")
    }
}
