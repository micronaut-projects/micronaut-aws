package io.micronaut.aws.sdk.v2.service


import software.amazon.awssdk.core.client.config.SdkClientOption
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Client

class S3ClientSpec extends ServiceClientSpec {

    @Override
    protected String serviceName() {
        return S3Client.SERVICE_NAME
    }

    void "it can configure an S3 client"() {
        when:
        S3Client client = applicationContext.getBean(S3Client)

        then:
        client.serviceName() == serviceName()
    }

    void "it can configure an S3 async client"() {
        when:
        S3AsyncClient client = applicationContext.getBean(S3AsyncClient)

        then:
        client.serviceName() == serviceName()
    }

    void "it can override the endpoint"() {
        when:
        S3Client client = applicationContext.getBean(S3Client)
        S3AsyncClient asyncClient = applicationContext.getBean(S3AsyncClient)

        then:
        client.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
        asyncClient.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
    }
}
