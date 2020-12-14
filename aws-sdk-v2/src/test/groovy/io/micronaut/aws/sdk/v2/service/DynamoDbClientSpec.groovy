package io.micronaut.aws.sdk.v2.service

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

class DynamoDbClientSpec extends ApplicationContextSpecification {

    void "it can configure a sync client"() {
        when:
        DynamoDbClient client = applicationContext.getBean(DynamoDbClient)

        then:
        client.serviceName() == DynamoDbClient.SERVICE_NAME
    }

    void "it can configure an async client"() {
        when:
        DynamoDbAsyncClient client = applicationContext.getBean(DynamoDbAsyncClient)

        then:
        client.serviceName() == DynamoDbClient.SERVICE_NAME
    }
}
