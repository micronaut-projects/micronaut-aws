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
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.net.URI;
import java.net.URISyntaxException;

/**
 * {@link EachProperty} implementation of {@link AWSServiceConfiguration} for {@literal aws.services.*} configuration.
 *
 * @author Stephen Cprek
 * @since 3.10.0
 *
 */
@EachProperty(AWSServiceConfigurationProperties.SERVICE_PREFIX)
public class AWSServiceConfigurationProperties implements AWSServiceConfiguration {

    /**
     * Prefix for all AWS Service Client settings.
     */
    public static final String SERVICE_PREFIX = AWSConfiguration.PREFIX + ".services";

    private final String serviceName;
    @Nullable
    private URI endpointOverride;

    public AWSServiceConfigurationProperties(@Parameter String serviceName)
        throws URISyntaxException {
        this.serviceName = serviceName;
    }

    /**
     * @return The endpoint with which the AWS SDK should communicate
     * @since 3.10.0
     */
    @Override
    @Nullable
    public URI getEndpointOverride() {
        return endpointOverride;
    }

    /**
     * @return The Service Name
     */
    @Override
    @NonNull
    public String getServiceName() {
        return serviceName;
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

