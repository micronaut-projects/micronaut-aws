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
package io.micronaut.aws.xray;

import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.annotation.ConfigurationProperties;
import java.util.Optional;

/**
 * {@link ConfigurationProperties} implementation of {@link XRayConfiguration}.
 * @author Sergio del Amo
 * @since 2.7.0
 */
@ConfigurationProperties(XRayConfigurationProperties.PREFIX)
public class XRayConfigurationProperties implements XRayConfiguration {
    public static final String PREFIX = "tracing.xray";

    /**
     * The default enable value.
     */
    @SuppressWarnings("WeakerAccess")
    public static final boolean DEFAULT_ENABLED = true;

    /**
     * The default serverFilter value.
     */
    @SuppressWarnings("WeakerAccess")
    public static final boolean DEFAULT_SERVER_FILTER = true;

    /**
     * The default clientFilter value.
     */
    @SuppressWarnings("WeakerAccess")
    public static final boolean DEFAULT_CLIENT_FILTER = true;

    /**
     * The default cloudWatchMetrics value.
     */
    @SuppressWarnings("WeakerAccess")
    public static final boolean DEFAULT_CLOUD_WATCH_METRICS = true;

    /**
     * The default sdkClients value.
     */
    @SuppressWarnings("WeakerAccess")
    public static final boolean DEFAULT_SDK_CLIENTS = true;

    @Nullable
    private String samplingRule;

    @Nullable
    private String segmentName;

    private boolean enabled = DEFAULT_ENABLED;

    private boolean serverFilter = DEFAULT_SERVER_FILTER;

    private boolean clientFilter = DEFAULT_CLIENT_FILTER;

    private boolean cloudWatchMetrics = DEFAULT_CLOUD_WATCH_METRICS;

    private boolean sdkClients = DEFAULT_SDK_CLIENTS;

    /**
     * @return Segment Name
     */
    @NonNull
    @Override
    public Optional<String> getSegmentName() {
        return Optional.ofNullable(segmentName);
    }

    /**
     * Segment name. Not set by default.
     * @param segmentName Segment Name
     */
    public void setSegmentName(@Nullable String segmentName) {
        this.segmentName = segmentName;
    }

    @Override
    @NonNull
    public Optional<String> getSamplingRule() {
        return Optional.ofNullable(samplingRule);
    }

    /**
     * Sampling Rule. Not set by default.
     * @param samplingRule Sampling rule
     */
    public void setSamplingRule(@Nullable String samplingRule) {
        this.samplingRule = samplingRule;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether this configuration is enabled. Default {@value #DEFAULT_ENABLED}.
     *
     * @param enabled The enabled setting
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public boolean isServerFilter() {
        return serverFilter;
    }

    /**
     * Whether the X-Ray HTTP Server filter is enabled. Default {@value #DEFAULT_SERVER_FILTER}.
     * @param serverFilter Whether the X-Ray HTTP Server filter is enabled
     */
    public void setServerFilter(boolean serverFilter) {
        this.serverFilter = serverFilter;
    }

    @Override
    public boolean isClientFilter() {
        return clientFilter;
    }

    /**
     * Whether the X-Ray HTTP Client filter is enabled. Default {@value #DEFAULT_CLIENT_FILTER}.
     * @param clientFilter Whether the X-Ray HTTP Client filter is enabled
     */
    public void setClientFilter(boolean clientFilter) {
        this.clientFilter = clientFilter;
    }

    @Override
    public boolean isCloudWatchMetrics() {
        return cloudWatchMetrics;
    }

    /**
     * Whether the X-Ray Cloud Watch Metrics integration is enabled. Default {@value #DEFAULT_CLOUD_WATCH_METRICS}.
     * @param cloudWatchMetrics Whether the X-Ray Cloud Watch Metrics integration is enabled
     */
    public void setCloudWatchMetrics(boolean cloudWatchMetrics) {
        this.cloudWatchMetrics = cloudWatchMetrics;
    }

    @Override
    public boolean isSdkClients() {
        return sdkClients;
    }

    /**
     * Whether X-Ray Tracing Interceptor should be configured for every AWS SDK Client builder. Default {@value #DEFAULT_SDK_CLIENTS}.
     * @param sdkClients Whether X-Ray Tracing Interceptor should be configured for every AWS SDK Client builder.
     */
    public void setSdkClients(boolean sdkClients) {
        this.sdkClients = sdkClients;
    }
}
