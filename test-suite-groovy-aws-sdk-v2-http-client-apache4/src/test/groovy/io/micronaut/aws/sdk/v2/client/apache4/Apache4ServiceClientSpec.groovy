package io.micronaut.aws.sdk.v2.client.apache4

import io.micronaut.context.BeanContext
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import software.amazon.awssdk.services.s3.S3Client
import spock.lang.Specification

import io.micronaut.context.annotation.Property

@Property(name = "aws.region", value = "eu-west-1")
@MicronautTest(startApplication = false)
class Apache4ServiceClientSpec extends Specification {
    @Inject
    BeanContext beanContext

    void "a sync service client is built when the apache 4 client is the only sync client on the classpath"() {
        when:
        S3Client client = beanContext.getBean(S3Client)

        then:
        client.serviceName() == S3Client.SERVICE_NAME
    }
}
