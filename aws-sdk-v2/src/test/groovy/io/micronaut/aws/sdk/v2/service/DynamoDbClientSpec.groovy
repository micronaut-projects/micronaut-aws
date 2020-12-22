package io.micronaut.aws.sdk.v2.service

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import software.amazon.awssdk.core.client.config.SdkClientOption
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient
import software.amazon.awssdk.services.dynamodb.DynamoDbClient
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest(rebuildContext = true)
class DynamoDbClientSpec extends Specification {
    @Inject
    ApplicationContext applicationContext

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

    @Property(name = "aws.dynamo.endpoint-override", value = "https://test.io")
    void "it can have custom endpoint configurations"() {
        when:
        DynamoDbClient client = applicationContext.getBean(DynamoDbClient)
        DynamoDbAsyncClient asyncClient = applicationContext.getBean(DynamoDbAsyncClient)
        ClientConfigurationProperties config = applicationContext.findBean(ClientConfigurationProperties, Qualifiers.byName("dynamo")).get()

        then:
        client.clientConfiguration.option(SdkClientOption.ENDPOINT) == URI.create("https//test.io")
        asyncClient.clientConfiguration.option(SdkClientOption.ENDPOINT) == URI.create("https//test.io")
    }
}
