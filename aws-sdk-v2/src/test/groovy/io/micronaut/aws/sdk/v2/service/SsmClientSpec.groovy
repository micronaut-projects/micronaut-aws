package io.micronaut.aws.sdk.v2.service

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.inject.qualifiers.Qualifiers
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import software.amazon.awssdk.services.ssm.SsmAsyncClient
import software.amazon.awssdk.services.ssm.SsmClient
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest(rebuildContext = true)
class SsmClientSpec extends Specification {
    @Inject
    ApplicationContext applicationContext

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

    @Property(name = "aws.ssm.endpoint-override", value = "https://test.io")
    void "it can have custom endpoint configurations"() {
        when:
        SsmClient client = applicationContext.getBean(SsmClient)
        SsmAsyncClient asyncClient = applicationContext.getBean(SsmAsyncClient)
        AwsClientConfiguration config = applicationContext.findBean(AwsClientConfiguration, Qualifiers.byName("ssm")).get()

        then:
        client != null
        asyncClient != null
        config.endpointOverride.get() == URI.create("https://test.io")
    }
}
