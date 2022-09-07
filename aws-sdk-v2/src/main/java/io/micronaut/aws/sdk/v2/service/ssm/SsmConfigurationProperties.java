package io.micronaut.aws.sdk.v2.service.ssm;

import io.micronaut.aws.sdk.v2.service.ClientConfigurationProperties;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.services.ssm.SsmClient;

@ConfigurationProperties(SsmClient.SERVICE_NAME)
public class SsmConfigurationProperties extends ClientConfigurationProperties {
}
