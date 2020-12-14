package io.micronaut.aws.sdk.v2.service

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification
import software.amazon.awssdk.services.ssm.SsmAsyncClient
import software.amazon.awssdk.services.ssm.SsmClient

class SsmClientSpec extends ApplicationContextSpecification {

    void "it can configure a sync client"() {
        when:
        SsmClient client = applicationContext.getBean(SsmClient)

        then:
        client.serviceName() == SsmClient.SERVICE_NAME
    }

    void "it can configure an async client"() {
        when:
        SsmAsyncClient client = applicationContext.getBean(SsmAsyncClient)

        then:
        client.serviceName() == SsmClient.SERVICE_NAME
    }
}
