package io.micronaut.aws.sdk.v2.service


import software.amazon.awssdk.core.client.config.SdkClientOption
import software.amazon.awssdk.services.ssm.SsmAsyncClient
import software.amazon.awssdk.services.ssm.SsmClient

class SsmClientSpec extends ServiceClientSpec {

    @Override
    protected String serviceName() {
        return SsmClient.SERVICE_NAME
    }

    void "it can configure a sync client"() {
        when:
        SsmClient client = applicationContext.getBean(SsmClient)

        then:
        client.serviceName() == serviceName()
    }

    void "it can configure an async client"() {
        when:
        SsmAsyncClient client = applicationContext.getBean(SsmAsyncClient)

        then:
        client.serviceName() == serviceName()
    }

    void "it can override the endpoint"() {
        when:
        SsmClient client = applicationContext.getBean(SsmClient)
        SsmAsyncClient asyncClient = applicationContext.getBean(SsmAsyncClient)

        then:
        client.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
        asyncClient.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
    }
}
