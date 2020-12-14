package io.micronaut.aws.sdk.v2.service

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.SqsClient

class SqsClientSpec extends ApplicationContextSpecification {

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
}
