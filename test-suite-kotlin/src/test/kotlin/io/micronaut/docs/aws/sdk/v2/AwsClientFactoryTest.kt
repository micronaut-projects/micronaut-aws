package io.micronaut.docs.aws.sdk.v2

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClient
import software.amazon.awssdk.services.rekognition.RekognitionClient

@Property(name = "spec.name", value = "AwsClientFactoryTest")
@MicronautTest(startApplication = false)
class AwsClientFactoryTest {

    @Inject
    lateinit var applicationContext: ApplicationContext

    @Test
    fun itCanCreateSyncClients() {
        val client = applicationContext.getBean(RekognitionClient::class.java)

        assertEquals(RekognitionClient.SERVICE_NAME, client.serviceName())
    }

    @Test
    fun itCanCreateAsyncClients() {
        val client = applicationContext.getBean(RekognitionAsyncClient::class.java)

        assertEquals(RekognitionClient.SERVICE_NAME, client.serviceName())
    }
}
