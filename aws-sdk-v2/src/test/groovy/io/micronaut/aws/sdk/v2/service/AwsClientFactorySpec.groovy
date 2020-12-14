package io.micronaut.aws.sdk.v2.service

import io.micronaut.aws.sdk.v2.ApplicationContextSpecification
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClient
import software.amazon.awssdk.services.rekognition.RekognitionClient

class AwsClientFactorySpec extends ApplicationContextSpecification {
    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                'spec.name': 'AwsClientFactorySpec'
        ]
    }

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
