package io.micronaut.aws.sdk.v2.service.gatewaymanagement;

import io.micronaut.aws.sdk.v2.service.ClientConfigurationProperties;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;

@ConfigurationProperties(ApiGatewayManagementApiClient.SERVICE_NAME)
public class ApiGatewayManagementApiConfigurationProperties extends ClientConfigurationProperties {
}
