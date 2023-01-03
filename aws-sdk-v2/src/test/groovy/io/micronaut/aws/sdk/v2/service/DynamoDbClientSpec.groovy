package io.micronaut.aws.sdk.v2.service

import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

class DynamoDbClientSpec extends ServiceClientSpec<DynamoDbClient, DynamoDbAsyncClient> {
    @Override
    protected String serviceName() {
        return DynamoDbClient.SERVICE_NAME
    }

    @Override
    protected DynamoDbClient getClient() {
        applicationContext.getBean(DynamoDbClient)
    }

    protected DynamoDbAsyncClient getAsyncClient() {
        applicationContext.getBean(DynamoDbAsyncClient)
    }
}
