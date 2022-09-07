package io.micronaut.aws.sdk.v2.service


import software.amazon.awssdk.core.client.config.SdkClientOption
import software.amazon.awssdk.services.ses.SesAsyncClient
import software.amazon.awssdk.services.ses.SesClient

class SesClientSpec extends ServiceClientSpec {

    @Override
    protected String serviceName() {
        return SesClient.SERVICE_NAME
    }

    void "it can configure a sync client"() {
        when:
        SesClient client = applicationContext.getBean(SesClient)

        then:
        client.serviceName() == serviceName()
    }

    void "it can configure an async client"() {
        when:
        SesAsyncClient client = applicationContext.getBean(SesAsyncClient)

        then:
        client.serviceName() == serviceName()
    }

    void "it can override the endpoint"() {
        when:
        SesClient client = applicationContext.getBean(SesClient)
        SesAsyncClient asyncClient = applicationContext.getBean(SesAsyncClient)

        then:
        client.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
        asyncClient.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
    }
}
