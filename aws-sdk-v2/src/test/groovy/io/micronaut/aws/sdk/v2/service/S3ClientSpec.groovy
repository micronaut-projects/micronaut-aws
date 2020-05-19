package io.micronaut.aws.sdk.v2.service

import io.micronaut.context.ApplicationContext
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.services.s3.S3Client
import spock.lang.Specification

class S3ClientSpec extends Specification{

    void "it can configure an S3 client"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run()

        when:
        S3Client client = applicationContext.getBean(S3Client)

        then:
        client.serviceName() == 's3'
    }

    void "it can configure an S3 async client"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run()

        when:
        S3AsyncClient client = applicationContext.getBean(S3AsyncClient)

        then:
        client.serviceName() == 's3'
    }

}
