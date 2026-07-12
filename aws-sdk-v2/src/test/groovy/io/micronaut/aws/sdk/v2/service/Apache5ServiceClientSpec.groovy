package io.micronaut.aws.sdk.v2.service

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification
import software.amazon.awssdk.services.s3.S3Client

class Apache5ServiceClientSpec extends ApplicationContextSpecification {

    void "a sync service client is built when the apache 5 client is the only sync client on the classpath"() {
        when:
        S3Client client = applicationContext.getBean(S3Client)

        then:
        client.serviceName() == S3Client.SERVICE_NAME
    }
}
