package io.micronaut.aws.sdk.v2;


import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.RekognitionClientBuilder;
import software.amazon.awssdk.services.s3.S3Configuration;

//tag::class[]
@ConfigurationProperties(RekognitionClient.SERVICE_NAME)
public class RekognitionConfigurationProperties  {

    @ConfigurationBuilder(prefixes = {""}, excludes = {"profileFile", "applyMutation"})
    private RekognitionClientBuilder builder = RekognitionClient.builder();

    /**
     * @return The builder
     */
    public RekognitionClientBuilder getBuilder() {
        return builder;
    }
}
//end::class[]
