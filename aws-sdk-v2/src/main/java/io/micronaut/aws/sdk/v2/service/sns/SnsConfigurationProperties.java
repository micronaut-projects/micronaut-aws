package io.micronaut.aws.sdk.v2.service.sns;

import io.micronaut.aws.sdk.v2.service.ClientConfigurationProperties;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.services.sns.SnsClient;

@ConfigurationProperties(SnsClient.SERVICE_NAME)
public class SnsConfigurationProperties extends ClientConfigurationProperties {
}
