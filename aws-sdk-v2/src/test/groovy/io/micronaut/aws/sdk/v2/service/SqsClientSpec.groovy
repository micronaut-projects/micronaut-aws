package io.micronaut.aws.sdk.v2.service


import software.amazon.awssdk.core.client.config.SdkClientOption
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.SqsClient

class SqsClientSpec extends ServiceClientSpec {

    @Override
    protected String serviceName() {
        return SqsClient.SERVICE_NAME
    }

    void "it can configure a sync client"() {
        when:
        SqsClient client = applicationContext.getBean(SqsClient)

        then:
        client.serviceName() == serviceName()
    }

    void "it can configure an async client"() {
        when:
        SqsAsyncClient client = applicationContext.getBean(SqsAsyncClient)

        then:
        client.serviceName() == serviceName()
    }

    void "it can override the endpoint"() {
        when:
        SqsClient client = applicationContext.getBean(SqsClient)
        SqsAsyncClient asyncClient = applicationContext.getBean(SqsAsyncClient)

        then:
        client.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
        asyncClient.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
    }
}
