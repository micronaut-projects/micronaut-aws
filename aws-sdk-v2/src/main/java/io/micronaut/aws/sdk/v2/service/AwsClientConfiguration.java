package io.micronaut.aws.sdk.v2.service;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;

import java.net.URI;
import java.util.Optional;

@EachProperty(value = "aws")
public class AwsClientConfiguration {

    private final String serviceName;
    private Optional<URI> endpointOverride = Optional.empty();

    public AwsClientConfiguration(@Parameter String serviceName) {
        this.serviceName = serviceName;
    }

    public Optional<URI> getEndpointOverride() {
        return endpointOverride;
    }

    public void setEndpointOverride(Optional<URI> endpointOverride) {
        this.endpointOverride = endpointOverride;
    }

    public String getServiceName() {
        return serviceName;
    }

    public static AwsClientConfiguration DEFAULT = new AwsClientConfiguration("default");
}
