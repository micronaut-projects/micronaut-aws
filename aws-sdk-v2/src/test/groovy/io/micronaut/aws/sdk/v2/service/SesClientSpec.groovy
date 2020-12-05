package io.micronaut.aws.sdk.v2.service

import io.micronaut.context.ApplicationContext
import software.amazon.awssdk.services.ses.SesAsyncClient
import software.amazon.awssdk.services.ses.SesClient
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class SesClientSpec extends Specification {
    @AutoCleanup
    @Shared
    ApplicationContext applicationContext = ApplicationContext.run()

    void "it can configure a sync client"() {
        when:
        SesClient client = applicationContext.getBean(SesClient)

        then:
        client.serviceName() == SesClient.SERVICE_NAME
    }

    void "it can configure an async client"() {
        when:
        SesAsyncClient client = applicationContext.getBean(SesAsyncClient)

        then:
        client.serviceName() == SesClient.SERVICE_NAME
    }
}
