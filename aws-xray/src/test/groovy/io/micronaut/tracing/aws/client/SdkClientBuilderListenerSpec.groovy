package io.micronaut.tracing.aws.client

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder
import software.amazon.awssdk.services.s3.S3Client
import software.amazon.awssdk.services.s3.S3ClientBuilder
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest(environments = Environment.AMAZON_EC2)
class SdkClientBuilderListenerSpec extends Specification {

    @Inject
    @Shared
    ApplicationContext context

    def "it configures xray to s3 client"(){
        expect:
        context.containsBean(S3AsyncClientBuilder.class)
        context.getBean(S3AsyncClientBuilder.class)
        context.containsBean(S3ClientBuilder.class)
        context.getBean(S3ClientBuilder.class)
    }
}
