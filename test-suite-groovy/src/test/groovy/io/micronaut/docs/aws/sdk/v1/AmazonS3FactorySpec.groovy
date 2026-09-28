package io.micronaut.docs.aws.sdk.v1

import com.amazonaws.services.s3.AmazonS3
import io.micronaut.aws.sdk.v1.EnvironmentAWSCredentialsProvider
import io.micronaut.context.annotation.Property
import io.micronaut.context.env.Environment
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@Property(name = "aws.access-key-id", value = "XXXX")
@Property(name = "aws.secret-key", value = "YYYY")
@MicronautTest(startApplication = false)
class AmazonS3FactorySpec extends Specification {

    @Inject
    AmazonS3 amazonS3

    @Inject
    Environment environment

    void "S3 client uses the credentials of the Micronaut environment"() {
        expect:
        amazonS3
        amazonS3.regionName == "us-east-1"

        when:
        EnvironmentAWSCredentialsProvider credentialsProvider = new EnvironmentAWSCredentialsProvider(environment)

        then:
        credentialsProvider.credentials.AWSAccessKeyId == "XXXX"
        credentialsProvider.credentials.AWSSecretKey == "YYYY"
    }
}
