package io.micronaut.aws.sdk.v2.service

import io.micronaut.context.ApplicationContext
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.SnsClient
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class SnsClientSpec extends Specification {
    @AutoCleanup
    @Shared
    ApplicationContext applicationContext = ApplicationContext.run()

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
