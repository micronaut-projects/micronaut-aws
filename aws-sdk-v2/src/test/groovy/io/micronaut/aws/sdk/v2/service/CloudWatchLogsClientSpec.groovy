package io.micronaut.aws.sdk.v2.service

import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsAsyncClient
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient

class CloudWatchLogsClientSpec extends ServiceClientSpec<CloudWatchLogsClient, CloudWatchLogsAsyncClient> {
    @Override
    protected String serviceName() {
        return CloudWatchLogsClient.SERVICE_NAME
    }

    @Override
    protected CloudWatchLogsClient getClient() {
        applicationContext.getBean(CloudWatchLogsClient)
    }

    protected CloudWatchLogsAsyncClient getAsyncClient() {
        applicationContext.getBean(CloudWatchLogsAsyncClient)
    }
}
