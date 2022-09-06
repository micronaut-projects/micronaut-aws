package io.micronaut.aws.sdk.v2.service

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsAsyncClient
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient

class CloudWatchLogsClientSpec extends ApplicationContextSpecification {

    void "it can configure a sync client"() {
        when:
        CloudWatchLogsClient client = applicationContext.getBean(CloudWatchLogsClient)

        then:
        client.serviceName() == CloudWatchLogsClient.SERVICE_NAME
    }

    void "it can configure an async client"() {
        when:
        CloudWatchLogsAsyncClient client = applicationContext.getBean(CloudWatchLogsAsyncClient)

        then:
        client.serviceName() == CloudWatchLogsClient.SERVICE_NAME
    }
}
