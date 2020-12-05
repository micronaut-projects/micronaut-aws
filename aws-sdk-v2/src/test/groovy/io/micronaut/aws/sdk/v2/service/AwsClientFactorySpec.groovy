package io.micronaut.aws.sdk.v2.service

import io.micronaut.context.ApplicationContext
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClient
import software.amazon.awssdk.services.rekognition.RekognitionClient
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class AwsClientFactorySpec extends Specification {
    @AutoCleanup
    @Shared
    ApplicationContext applicationContext = ApplicationContext.run([
            'spec.name': 'AwsClientFactorySpec'
    ])

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
