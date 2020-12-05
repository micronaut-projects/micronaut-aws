package io.micronaut.aws.sdk.v2.service

import io.micronaut.context.ApplicationContext
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import software.amazon.awssdk.core.client.config.ClientOverrideConfiguration
import software.amazon.awssdk.core.client.config.SdkClientOption
import software.amazon.awssdk.core.retry.RetryMode
import software.amazon.awssdk.core.retry.RetryPolicy
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3ClientBuilder
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Singleton

class S3ClientSpec extends Specification {
    @AutoCleanup
    @Shared
    ApplicationContext applicationContext = ApplicationContext.run()

    void "it can configure an S3 client"() {
        when:
        S3Client client = applicationContext.getBean(S3Client)

        then:
        client.serviceName() == S3Client.SERVICE_NAME
    }

    void "it can configure an S3 async client"() {
        when:
        S3AsyncClient client = applicationContext.getBean(S3AsyncClient)

        then:
        client.serviceName() == S3Client.SERVICE_NAME
    }

    void "builders can be customised"() {
        given:
        ApplicationContext applicationContextWithCustomBuilder = ApplicationContext.run([
                'spec.name': 'S3ClientSpec.builders'
        ])

        when:
        S3ClientBuilder builder = applicationContextWithCustomBuilder.getBean(S3ClientBuilder)

        then:
        builder.clientConfiguration.attributes.configuration.get(SdkClientOption.RETRY_POLICY).retryMode() == RetryMode.LEGACY

        cleanup:
        applicationContextWithCustomBuilder.close()
    }
}
