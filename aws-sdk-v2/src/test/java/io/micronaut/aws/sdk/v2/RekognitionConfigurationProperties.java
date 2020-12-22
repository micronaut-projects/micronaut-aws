package io.micronaut.aws.sdk.v2;

import io.micronaut.aws.sdk.v2.service.ClientConfigurationProperties;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.services.rekognition.RekognitionClient;

@ConfigurationProperties(RekognitionClient.SERVICE_NAME)
public class RekognitionConfigurationProperties extends ClientConfigurationProperties {
}
