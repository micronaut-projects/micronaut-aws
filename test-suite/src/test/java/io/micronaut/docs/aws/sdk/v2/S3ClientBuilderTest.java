package io.micronaut.docs.aws.sdk.v2;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Property;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.core.retry.RetryMode;
import software.amazon.awssdk.core.retry.RetryPolicy;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@Property(name = "spec.name", value = "S3ClientBuilderTest")
@MicronautTest(startApplication = false)
class S3ClientBuilderTest {

    @Inject
    ApplicationContext applicationContext;

    @Test
    void buildersCanBeCustomised() {
        S3ClientBuilder builder = applicationContext.getBean(S3ClientBuilder.class);

        assertTrue(builder.overrideConfiguration().retryPolicy().isPresent());
        assertEquals(RetryMode.LEGACY, builder.overrideConfiguration().retryPolicy().map(RetryPolicy::retryMode).orElse(null));
    }
}
