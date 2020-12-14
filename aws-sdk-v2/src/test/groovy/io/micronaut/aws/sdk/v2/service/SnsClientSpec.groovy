package io.micronaut.aws.sdk.v2.service

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.SnsClient

class SnsClientSpec extends ApplicationContextSpecification {

    void "it can configure a sync client"() {
        when:
        SnsClient client = applicationContext.getBean(SnsClient)

        then:
        client.serviceName() == SnsClient.SERVICE_NAME
    }

    void "it can configure an async client"() {
        when:
        SnsAsyncClient client = applicationContext.getBean(SnsAsyncClient)

        then:
        client.serviceName() == SnsClient.SERVICE_NAME
    }
}
