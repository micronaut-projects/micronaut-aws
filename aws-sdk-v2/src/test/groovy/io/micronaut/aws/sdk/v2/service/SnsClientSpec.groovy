package io.micronaut.aws.sdk.v2.service

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import software.amazon.awssdk.core.client.config.SdkClientOption
import software.amazon.awssdk.services.sns.SnsAsyncClient
import software.amazon.awssdk.services.sns.SnsClient
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest(rebuildContext = true)
class SnsClientSpec extends Specification {
    @Inject
    ApplicationContext applicationContext

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

    @Property(name = "aws.sns.endpoint-override", value = "https://test.io")
    void "it can have custom endpoint configurations"() {
        when:
        SnsClient client = applicationContext.getBean(SnsClient)
        SnsAsyncClient asyncClient = applicationContext.getBean(SnsAsyncClient)
        ClientConfigurationProperties config = applicationContext.findBean(ClientConfigurationProperties, Qualifiers.byName("sns")).get()

        then:
        client.clientConfiguration.option(SdkClientOption.ENDPOINT) == URI.create("https//test.io")
        asyncClient.clientConfiguration.option(SdkClientOption.ENDPOINT) == URI.create("https//test.io")
    }
}
