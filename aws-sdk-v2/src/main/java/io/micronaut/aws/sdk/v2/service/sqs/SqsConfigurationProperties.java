package io.micronaut.aws.sdk.v2.service.sqs;

import io.micronaut.aws.sdk.v2.service.ClientConfigurationProperties;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.services.sqs.SqsClient;

@ConfigurationProperties(SqsClient.SERVICE_NAME)
public class SqsConfigurationProperties extends ClientConfigurationProperties {
}
