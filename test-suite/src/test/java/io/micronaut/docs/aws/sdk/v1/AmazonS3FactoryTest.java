package io.micronaut.docs.aws.sdk.v1;

import com.amazonaws.services.s3.AmazonS3;
import io.micronaut.aws.sdk.v1.EnvironmentAWSCredentialsProvider;
import io.micronaut.context.annotation.Property;
import io.micronaut.context.env.Environment;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@Property(name = "aws.access-key-id", value = "XXXX")
@Property(name = "aws.secret-key", value = "YYYY")
@MicronautTest(startApplication = false)
class AmazonS3FactoryTest {

    @Inject
    AmazonS3 amazonS3;

    @Inject
    Environment environment;

    @Test
    void s3ClientUsesTheCredentialsOfTheMicronautEnvironment() {
        assertNotNull(amazonS3);
        assertEquals("us-east-1", amazonS3.getRegionName());

        EnvironmentAWSCredentialsProvider credentialsProvider = new EnvironmentAWSCredentialsProvider(environment);
        assertEquals("XXXX", credentialsProvider.getCredentials().getAWSAccessKeyId());
        assertEquals("YYYY", credentialsProvider.getCredentials().getAWSSecretKey());
    }
}
