package io.micronaut.aws.sdk.v2.service

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.SqsClient
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest(rebuildContext = true)
class SqsClientSpec extends Specification {
    @Inject
    ApplicationContext applicationContext

    void "it can configure a sync client"() {
        when:
        SqsClient client = applicationContext.getBean(SqsClient)

        then:
        client.serviceName() == SqsClient.SERVICE_NAME
    }

    void "it can configure an async client"() {
        when:
        SqsAsyncClient client = applicationContext.getBean(SqsAsyncClient)

        then:
        client.serviceName() == SqsClient.SERVICE_NAME
    }

    @Property(name = "aws.sqs.endpoint-override", value = "https://test.io")
    void "it can have custom endpoint configurations"() {
        when:
        SqsClient client = applicationContext.getBean(SqsClient)
        SqsAsyncClient asyncClient = applicationContext.getBean(SqsAsyncClient)
        AwsClientConfiguration config = applicationContext.findBean(AwsClientConfiguration, Qualifiers.byName("sqs")).get()

        then:
        client != null
        asyncClient != null
        config.endpointOverride.get() == URI.create("https://test.io")
    }
}
