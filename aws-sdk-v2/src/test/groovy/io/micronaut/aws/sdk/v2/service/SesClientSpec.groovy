package io.micronaut.aws.sdk.v2.service

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import software.amazon.awssdk.core.client.config.SdkClientOption
import software.amazon.awssdk.services.ses.SesAsyncClient
import software.amazon.awssdk.services.ses.SesClient
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest(rebuildContext = true)
class SesClientSpec extends Specification {
    @Inject
    ApplicationContext applicationContext

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

    @Property(name = "aws.ses.endpoint-override", value = "https://test.io")
    void "it can have custom endpoint configurations"() {
        when:
        SesClient client = applicationContext.getBean(SesClient)
        SesAsyncClient asyncClient = applicationContext.getBean(SesAsyncClient)

        then:
        client.clientConfiguration.option(SdkClientOption.ENDPOINT) == URI.create("https//test.io")
        asyncClient.clientConfiguration.option(SdkClientOption.ENDPOINT) == URI.create("https//test.io")
    }
}
