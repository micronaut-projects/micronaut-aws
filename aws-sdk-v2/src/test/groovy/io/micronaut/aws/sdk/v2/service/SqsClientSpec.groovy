package io.micronaut.aws.sdk.v2.service

import io.micronaut.context.ApplicationContext
import software.amazon.awssdk.services.sqs.SqsAsyncClient
import software.amazon.awssdk.services.sqs.SqsClient
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class SqsClientSpec extends Specification {
    @AutoCleanup
    @Shared
    ApplicationContext applicationContext = ApplicationContext.run()

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
