package io.micronaut.aws.sdk.v2.service


import software.amazon.awssdk.core.client.config.SdkClientOption
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsAsyncClient
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient

class CloudWatchLogsClientSpec extends ServiceClientSpec {

    @Override
    protected String serviceName() {
        return CloudWatchLogsClient.SERVICE_NAME
    }

    void "it can configure a sync client"() {
        when:
        CloudWatchLogsClient client = applicationContext.getBean(CloudWatchLogsClient)

        then:
        client.serviceName() == serviceName()
    }

    void "it can configure an async client"() {
        when:
        CloudWatchLogsAsyncClient client = applicationContext.getBean(CloudWatchLogsAsyncClient)

        then:
        client.serviceName() == serviceName()
    }

    void "it can override the endpoint"() {
        when:
        CloudWatchLogsClient client = applicationContext.getBean(CloudWatchLogsClient)
        CloudWatchLogsAsyncClient asyncClient = applicationContext.getBean(CloudWatchLogsAsyncClient)

        then:
        client.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
        asyncClient.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
    }
}
