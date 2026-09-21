package io.micronaut.docs.aws.sdk.v2;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClient;
import software.amazon.awssdk.services.rekognition.RekognitionClient;

import static org.junit.jupiter.api.Assertions.assertEquals;

@Property(name = "spec.name", value = "AwsClientFactoryTest")
@MicronautTest(startApplication = false)
class AwsClientFactoryTest {

    @Inject
    ApplicationContext applicationContext;

    @Test
    void itCanCreateSyncClients() {
        RekognitionClient client = applicationContext.getBean(RekognitionClient.class);

        assertEquals(RekognitionClient.SERVICE_NAME, client.serviceName());
    }

    @Test
    void itCanCreateAsyncClients() {
        RekognitionAsyncClient client = applicationContext.getBean(RekognitionAsyncClient.class);

        assertEquals(RekognitionClient.SERVICE_NAME, client.serviceName());
    }
}
