package io.micronaut.aws.sdk.v2.service.secretsmanager;

import io.micronaut.aws.sdk.v2.service.ClientConfigurationProperties;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;

@ConfigurationProperties(SecretsManagerClient.SERVICE_NAME)
public class SecretsManagerConfigurationProperties extends ClientConfigurationProperties {
}
