package io.micronaut.aws.sdk.v2.service

import io.micronaut.context.ApplicationContext
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClient
import software.amazon.awssdk.services.rekognition.RekognitionClient
import spock.lang.Specification

class AwsClientFactorySpec extends Specification {

    void "it can create sync clients"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run([
                'spec.name': 'AwsClientFactorySpec'
        ])

        when:
        RekognitionClient client = applicationContext.getBean(RekognitionClient)

        then:
        client.serviceName() == RekognitionClient.SERVICE_NAME
    }

    void "it can create async clients"() {
        given:
        ApplicationContext applicationContext = ApplicationContext.run([
                'spec.name': 'AwsClientFactorySpec'
        ])

        when:
        RekognitionAsyncClient client = applicationContext.getBean(RekognitionAsyncClient)

        then:
        client.serviceName() == RekognitionClient.SERVICE_NAME
    }

}
