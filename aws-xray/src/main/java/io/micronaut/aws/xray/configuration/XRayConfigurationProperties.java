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
package io.micronaut.aws.xray.configuration;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.context.annotation.ConfigurationProperties;

import java.util.List;
import java.util.Optional;

/**
 * {@link ConfigurationProperties} implementation of {@link XRayConfiguration}.
 * @author Sergio del Amo
 * @since 3.2.0
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

    /**
     * The default user segment decorator value.
     */
    @SuppressWarnings("WeakerAccess")
    public static final boolean DEFAULT_USER_SEGMENT_DECORATOR = true;

    /**
     * The default remove trace id headers value value.
     */
    @SuppressWarnings("WeakerAccess")
    public static final boolean  DEFAULT_ACCEPT_TRACE_ID_HEADERS = true;

    @Nullable
    private List<String> excludes;

    @Nullable
    private String samplingRule;

    private boolean enabled = DEFAULT_ENABLED;

    private boolean serverFilter = DEFAULT_SERVER_FILTER;

    private boolean clientFilter = DEFAULT_CLIENT_FILTER;

    private boolean cloudWatchMetrics = DEFAULT_CLOUD_WATCH_METRICS;

    private boolean sdkClients = DEFAULT_SDK_CLIENTS;

    private boolean userSegmentDecorator = DEFAULT_USER_SEGMENT_DECORATOR;

    @Nullable
    private String fixedName;

    private boolean acceptTraceIdHeaders = DEFAULT_ACCEPT_TRACE_ID_HEADERS;

    @Override
    @NonNull
    public Optional<String> getFixedName() {
        return Optional.ofNullable(fixedName);
    }

    @Override
    @NonNull
    public Optional<List<String>> getExcludes() {
        return Optional.ofNullable(excludes);
    }

    /**
     *
     * @param excludes A list of paths which should not be filter by {@link io.micronaut.aws.xray.filters.server.XRayHttpServerFilter}.
     */
    public void setExcludes(@Nullable List<String> excludes) {
        this.excludes = excludes;
    }

    /**
     * @param fixedName Fixed segment name.
     */
    public void setFixedName(String fixedName) {
        this.fixedName = fixedName;
    }

    @Override
    @NonNull
    public Optional<String> getSamplingRule() {
        return Optional.ofNullable(samplingRule);
    }

    /**
     * A path either starting with `classpath:` or `file:` to sampling-rules file. You can serve files from anywhere on disk or the classpath. For example to serve a static resources from `src/main/resources/sampling-rules.json`, you would use `classpath:sampling-rules.json`.
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

    @Override
    public boolean isUserSegmentDecorator() {
        return userSegmentDecorator;
    }

    @Override
    public boolean acceptTraceIdHeaders() {
        return acceptTraceIdHeaders;
    }

    /**
     * Whether {@link io.micronaut.aws.xray.decorators.UserSegmentDecorator} should be loaded. Default {@value #DEFAULT_USER_SEGMENT_DECORATOR}.
     * @param userSegmentDecorator Whether {@link io.micronaut.aws.xray.decorators.UserSegmentDecorator} should be loaded.
     */
    public void setUserSegmentDecorator(boolean userSegmentDecorator) {
        this.userSegmentDecorator = userSegmentDecorator;
    }

    /**
     *
     * @return Whether the header {@code X-Amzn-Trace-Id} should be removed from incoming requests to avoid issues caused by users adding trace IDs or sampling decisions to their request.
     */
    public boolean isAcceptTraceIdHeaders() {
        return acceptTraceIdHeaders;
    }

    /**
     * Whether it should accept the HTTP header X-Amzn-Trace-Id from incoming requests. As a security measurement, you may choose not to accept incoming Trace ID Headers in gateway services to avoid issues caused by users adding trace IDs or sampling decisions to their request. Default value {@value #DEFAULT_ACCEPT_TRACE_ID_HEADERS}.
     * @param acceptTraceIdHeaders Whether the header {@code X-Amzn-Trace-Id} should be removed from incoming requests to avoid issues caused by users adding trace IDs or sampling decisions to their request.
     */
    public void setAcceptTraceIdHeaders(boolean acceptTraceIdHeaders) {
        this.acceptTraceIdHeaders = acceptTraceIdHeaders;
    }
}
