package io.micronaut.aws.sdk.v2.service

import io.micronaut.context.ApplicationContext
import software.amazon.awssdk.services.ses.SesAsyncClient
import software.amazon.awssdk.services.ses.SesClient
import spock.lang.Specification

class SesClientSpec extends Specification{

    void "it can configure a sync client"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run()

        when:
        SesClient client = applicationContext.getBean(SesClient)

        then:
        client.serviceName() == SesClient.SERVICE_NAME
    }

    void "it can configure an async client"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run()

        when:
        SesAsyncClient client = applicationContext.getBean(SesAsyncClient)

        then:
        client.serviceName() == SesClient.SERVICE_NAME
    }

}
