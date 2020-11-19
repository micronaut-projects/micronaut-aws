package io.micronaut.aws.sdk.v2.service

import io.micronaut.context.ApplicationContext
import software.amazon.awssdk.services.ssm.SsmAsyncClient
import software.amazon.awssdk.services.ssm.SsmClient
import spock.lang.Specification

class SsmClientSpec extends Specification{

    void "it can configure a sync client"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run()

        when:
        SsmClient client = applicationContext.getBean(SsmClient)

        then:
        client.serviceName() == SsmClient.SERVICE_NAME
    }

    void "it can configure an async client"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run()

        when:
        SsmAsyncClient client = applicationContext.getBean(SsmAsyncClient)

        then:
        client.serviceName() == SsmClient.SERVICE_NAME
    }

}
