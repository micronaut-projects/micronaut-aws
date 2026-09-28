package io.micronaut.docs.aws.sdk.v1

import com.amazonaws.services.s3.AmazonS3
import io.micronaut.aws.sdk.v1.EnvironmentAWSCredentialsProvider
import io.micronaut.context.annotation.Property
import io.micronaut.context.env.Environment
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

@Property(name = "aws.access-key-id", value = "XXXX")
@Property(name = "aws.secret-key", value = "YYYY")
@MicronautTest(startApplication = false)
class AmazonS3FactoryTest {

    @Inject
    lateinit var amazonS3: AmazonS3

    @Inject
    lateinit var environment: Environment

    @Test
    fun s3ClientUsesTheCredentialsOfTheMicronautEnvironment() {
        assertNotNull(amazonS3)
        assertEquals("us-east-1", amazonS3.regionName)

        val credentialsProvider = EnvironmentAWSCredentialsProvider(environment)
        assertEquals("XXXX", credentialsProvider.credentials.awsAccessKeyId)
        assertEquals("YYYY", credentialsProvider.credentials.awsSecretKey)
    }
}
