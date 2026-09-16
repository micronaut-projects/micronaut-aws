package io.micronaut.docs.aws.sdk.v2

import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test
import software.amazon.awssdk.core.retry.RetryMode
import software.amazon.awssdk.services.s3.S3ClientBuilder

@Property(name = "spec.name", value = "S3ClientBuilderTest")
@MicronautTest(startApplication = false)
class S3ClientBuilderTest {

    @Inject
    lateinit var applicationContext: ApplicationContext

    @Test
    fun buildersCanBeCustomised() {
        val builder = applicationContext.getBean(S3ClientBuilder::class.java)

        assertTrue(builder.overrideConfiguration().retryPolicy().isPresent)
        assertEquals(RetryMode.LEGACY, builder.overrideConfiguration().retryPolicy().map { it.retryMode() }.orElse(null))
    }
}
