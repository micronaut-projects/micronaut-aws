package io.micronaut.aws.sdk.v2.service.ses;

import io.micronaut.aws.sdk.v2.service.ClientConfigurationProperties;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.services.ses.SesClient;

@ConfigurationProperties(SesClient.SERVICE_NAME)
public class SesConfigurationProperties extends ClientConfigurationProperties {
}
