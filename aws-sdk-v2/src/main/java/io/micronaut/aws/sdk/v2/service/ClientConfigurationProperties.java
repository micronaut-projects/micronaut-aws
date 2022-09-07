package io.micronaut.aws.sdk.v2.service;

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.core.annotation.Nullable;

import java.net.URI;

public abstract class ClientConfigurationProperties extends AWSConfiguration {
    @Nullable
    private URI endpointOverride;

    /**
     * @return The endpoint with which the AWS SDK should communicate
     * @since 3.6.2
     */
    @Nullable
    public URI getEndpointOverride() {
        return endpointOverride;
    }

    /**
     * Provide a URI to override the endpoint with which the AWS SDK should communicate. Optional. Defaults to `null`.
     * @param endpointOverride The endpoint with which the AWS SDK should communicate
     * @since 3.6.2
     */
    public void setEndpointOverride(@Nullable URI endpointOverride) {
        this.endpointOverride = endpointOverride;
    }
}
