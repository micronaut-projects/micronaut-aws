/*
 * Copyright 2017-2022 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.aws.sdk.v2.service;

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.aws.AWSServiceConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.Nullable;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * Configuration class to host common AWS service client configuration properties.
 *
 * @author Stephen Cprek
 * @since 3.10.0
 *
 */
@EachProperty(AWSServiceConfigurationProperties.SERVICE_PREFIX)
public class AWSServiceConfiguration extends AWSConfiguration {

    private final String serviceName;
    @Nullable
    private URI endpointOverride;

    public AWSServiceConfiguration(@Parameter String serviceName)
        throws URISyntaxException {
        this.serviceName = serviceName;
    }

    /**
     * @return The Service Name
     */
    public String getServiceName() {
        return serviceName;
    }

    /**
     * @return The endpoint with which the AWS SDK should communicate
     * @since 3.10.0
     */
    @Nullable
    public URI getEndpointOverride() {
        return endpointOverride;
    }

    /**
     * Provide a URI to override the endpoint with which the AWS SDK should communicate. Optional. Defaults to `null`.
     * @param endpointOverride The endpoint with which the AWS SDK should communicate
     * @since 3.10.0
     */
    public void setEndpointOverride(@Nullable URI endpointOverride) {
        this.endpointOverride = endpointOverride;
    }
}
