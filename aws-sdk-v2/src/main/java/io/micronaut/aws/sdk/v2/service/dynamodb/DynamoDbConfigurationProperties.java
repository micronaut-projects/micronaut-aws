package io.micronaut.aws.sdk.v2.service.dynamodb;

import io.micronaut.aws.sdk.v2.service.ClientConfigurationProperties;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;

@ConfigurationProperties(DynamoDbClient.SERVICE_NAME)
public class DynamoDbConfigurationProperties extends ClientConfigurationProperties {
}
