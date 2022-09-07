package io.micronaut.aws.sdk.v2.service


import software.amazon.awssdk.core.client.config.SdkClientOption
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.SnsClient

class SnsClientSpec extends ServiceClientSpec {

    @Override
    protected String serviceName() {
        return SnsClient.SERVICE_NAME
    }

    void "it can configure a sync client"() {
        when:
        SnsClient client = applicationContext.getBean(SnsClient)

        then:
        client.serviceName() == serviceName()
    }

    void "it can configure an async client"() {
        when:
        SnsAsyncClient client = applicationContext.getBean(SnsAsyncClient)

        then:
        client.serviceName() == serviceName()
    }

    void "it can override the endpoint"() {
        when:
        SnsClient client = applicationContext.getBean(SnsClient)
        SnsAsyncClient asyncClient = applicationContext.getBean(SnsAsyncClient)

        then:
        client.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
        asyncClient.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
    }
}
