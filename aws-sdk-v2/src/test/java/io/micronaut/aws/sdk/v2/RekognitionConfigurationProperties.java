package io.micronaut.aws.sdk.v2;

import io.micronaut.aws.sdk.v2.service.ServiceClientConfiguration;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.services.rekognition.RekognitionClient;

//tag::class[]
@ConfigurationProperties(RekognitionClient.SERVICE_NAME)
public class RekognitionConfigurationProperties extends ServiceClientConfiguration {
}
//end::class[]
