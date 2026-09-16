package io.micronaut.docs.aws.sdk.v2

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClient
import software.amazon.awssdk.services.rekognition.RekognitionClient
import spock.lang.Specification

@Property(name = "spec.name", value = "AwsClientFactorySpec")
@MicronautTest(startApplication = false)
class AwsClientFactorySpec extends Specification {

    @Inject
    ApplicationContext applicationContext

    void "it can create sync clients"() {
        when:
        RekognitionClient client = applicationContext.getBean(RekognitionClient)

        then:
        client.serviceName() == RekognitionClient.SERVICE_NAME
    }

    void "it can create async clients"() {
        when:
        RekognitionAsyncClient client = applicationContext.getBean(RekognitionAsyncClient)

        then:
        client.serviceName() == RekognitionClient.SERVICE_NAME
    }
}
