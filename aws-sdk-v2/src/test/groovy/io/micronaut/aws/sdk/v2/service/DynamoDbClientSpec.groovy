package io.micronaut.aws.sdk.v2.service


import software.amazon.awssdk.core.client.config.SdkClientOption
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClient

class DynamoDbClientSpec extends ServiceClientSpec {

    @Override
    protected String serviceName() {
        return DynamoDbClient.SERVICE_NAME
    }

    void "it can configure a sync client"() {
        when:
        DynamoDbClient client = applicationContext.getBean(DynamoDbClient)

        then:
        client.serviceName() == serviceName()
    }

    void "it can configure an async client"() {
        when:
        DynamoDbAsyncClient client = applicationContext.getBean(DynamoDbAsyncClient)

        then:
        client.serviceName() == serviceName()
    }


    void "it can override the endpoint"() {
        when:
        DynamoDbClient client = applicationContext.getBean(DynamoDbClient)
        DynamoDbAsyncClient asyncClient = applicationContext.getBean(DynamoDbAsyncClient)

        then:
        client.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
        asyncClient.clientConfiguration.option(SdkClientOption.ENDPOINT).toString() == ENDPOINT
    }
}
