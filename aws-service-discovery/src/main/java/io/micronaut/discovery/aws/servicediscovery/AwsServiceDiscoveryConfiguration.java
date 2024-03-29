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
package io.micronaut.discovery.aws.servicediscovery;

import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.util.StringUtils;
import io.micronaut.discovery.DiscoveryConfiguration;

/**
 * @author Rvanderwerf
 * @since 1.0
 * See https://docs.aws.amazon.com/Route53/latest/APIReference/overview-service-discovery.html for details info
 */
@Internal
@Requires(env = Environment.AMAZON_EC2)
@Requires(property = AwsServiceDiscoveryConfiguration.ENABLED, value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
@ConfigurationProperties(AwsServiceDiscoveryConfiguration.PREFIX)
public class AwsServiceDiscoveryConfiguration extends DiscoveryConfiguration {

    public static final String PREFIX = "aws.service-discovery";

    /**
     * Configuration property for whether discovery is enabled.
     */
    public static final String ENABLED = AwsServiceDiscoveryConfiguration.PREFIX + ".enabled";

    private String awsServiceId;

    /**
     * AWS Service ID.
     * @return service id
     */
    public String getAwsServiceId() {
        return awsServiceId;
    }

    /**
     * set service ID for easier testing.
     * @param awsServiceId aws service id
     */
    public void setAwsServiceId(String awsServiceId) {
        this.awsServiceId = awsServiceId;
    }

    /**
     * enable/disable this feature.
     * @return enabled
     */
    @Override
    public boolean isEnabled() {
        return super.isEnabled();
    }

    /**
     * enable/disabled this feature. Default value ({@value io.micronaut.discovery.DiscoveryConfiguration#DEFAULT_ENABLED}).
     * @param enabled Whether discovery is enabled
     */
    @Override
    public void setEnabled(boolean enabled) {
        super.setEnabled(enabled);
    }
}
