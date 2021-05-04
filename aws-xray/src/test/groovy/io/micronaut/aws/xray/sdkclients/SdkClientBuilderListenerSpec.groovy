package io.micronaut.aws.xray.sdkclients

import io.micronaut.aws.xray.ApplicationContextSpecification
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder
import software.amazon.awssdk.services.s3.S3ClientBuilder

class SdkClientBuilderListenerSpec extends ApplicationContextSpecification {

    def "it configures xray to s3 client"(){
        expect:
        applicationContext.containsBean(S3AsyncClientBuilder)
        applicationContext.getBean(S3AsyncClientBuilder)
        applicationContext.containsBean(S3ClientBuilder)
        applicationContext.getBean(S3ClientBuilder)
    }
}
