package io.micronaut.aws.sdk.v2.service.servicediscovery;

import io.micronaut.aws.sdk.v2.service.ClientConfigurationProperties;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryClient;

@ConfigurationProperties(ServiceDiscoveryClient.SERVICE_NAME)
public class ServiceDiscoveryConfigurationProperties extends ClientConfigurationProperties {
}
