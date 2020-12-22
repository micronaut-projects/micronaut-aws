package io.micronaut.aws.sdk.v2.service;

import io.micronaut.aws.AWSConfiguration;

import java.net.URI;
import java.util.Optional;

public abstract class ClientConfigurationProperties extends AWSConfiguration {
    private Optional<URI> endpointOverride = Optional.empty();

    public Optional<URI> getEndpointOverride() {
        return endpointOverride;
    }

    public void setEndpointOverride(Optional<URI> endpointOverride) {
        this.endpointOverride = endpointOverride;
    }
}
