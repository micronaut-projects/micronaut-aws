/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.aws.sdk.v2.service.lambda;

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Inject;
import software.amazon.awssdk.metrics.MetricPublisher;
import software.amazon.awssdk.services.lambda.LambdaClient;

import java.net.URI;
import java.util.Collections;
import java.util.List;

/**
 * Configuration properties for Lambda.
 *
 * @since 3.8.0
 */
@ConfigurationProperties(LambdaClient.SERVICE_NAME)
public class LambdaConfigurationProperties extends AWSConfiguration {

    @Nullable
    private URI endpointOverride;

    private List<MetricPublisher> metricsPublishers = Collections.emptyList();

    /**
     * The metric publishers
     *
     * @return list of configured metric publishers
     */
    public List<MetricPublisher> getMetricsPublishers() {
        return metricsPublishers;
    }

    /**
     * @param metricsPublishers The {@link MetricPublisher}s
     */
    @Inject
    public void setMetricsPublishers(List<MetricPublisher> metricsPublishers) {
        this.metricsPublishers = metricsPublishers;
    }

    /**
     * @return The endpoint with which the AWS SDK should communicate
     */
    @Nullable
    public URI getEndpointOverride() {
        return endpointOverride;
    }

    /**
     * Provide a URI to override the endpoint with which the AWS SDK should communicate. Optional. Defaults to `null`.
     *
     * @param endpointOverride The endpoint with which the AWS SDK should communicate
     */
    public void setEndpointOverride(@Nullable URI endpointOverride) {
        this.endpointOverride = endpointOverride;
    }
}
