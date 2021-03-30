/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.tracing.aws;

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.bind.annotation.Bindable;

import java.util.Optional;

/**
 * Configuration for AWS x-ray.
 *
 * @author Pavol Gressa
 * @since 2.5
 */
@ConfigurationProperties(XRayConfiguration.PREFIX)
public interface XRayConfiguration {

    String PREFIX = AWSConfiguration.PREFIX + ".xray";

    Optional<String> getSamplingRule();

    @Bindable(defaultValue = "true")
    boolean isEnabled();

    /**
     * The sdk clients configuration.
     */
    @ConfigurationProperties(SdkClientsConfiguration.PREFIX)
    interface SdkClientsConfiguration {
        String PREFIX = "sdkclients";

        @Bindable(defaultValue = "true")
        boolean isEnabled();
    }

    /**
     * The cloud watch configuration.
     */
    @ConfigurationProperties(XRayCloudWatchMetricsConfiguration.PREFIX)
    interface XRayCloudWatchMetricsConfiguration {
        String PREFIX = "cloudwatch";

        @Bindable(defaultValue = "true")
        boolean isEnabled();
    }

    /**
     * The http filters configuration.
     */
    @ConfigurationProperties(XRayHttpFilterConfiguration.PREFIX)
    interface XRayHttpFilterConfiguration {
        String PREFIX = "httpfilter";

        @Bindable(defaultValue = "true")
        boolean isEnabled();


        /**
         * The server http filter configuration.
         */
        @ConfigurationProperties(XRayHttpServerFilterConfiguration.PREFIX)
        interface XRayHttpServerFilterConfiguration {
            String PREFIX = "server";

            Optional<String> getFixedSegmentName();

            @Bindable(defaultValue = "true")
            boolean isEnabled();
        }

        /**
         * The client http filter configuration.
         */
        @ConfigurationProperties(XRayHttpClientFilterConfiguration.PREFIX)
        interface XRayHttpClientFilterConfiguration {
            String PREFIX = "client";

            @Bindable(defaultValue = "true")
            boolean isEnabled();
        }
    }
}
