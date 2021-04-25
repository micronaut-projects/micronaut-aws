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
package io.micronaut.tracing.aws.configuration;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * HTTP Filter configuration.
 */
@ConfigurationProperties(XRayHttpFilterConfigurationProperties.PREFIX)
public class XRayHttpFilterConfigurationProperties implements XRayHttpFilterConfiguration {
    public static final String PREFIX = XRayConfigurationProperties.PREFIX + ".http-filter";
    private static final boolean DEFAULT_ENABLED = true;

    private boolean enabled = DEFAULT_ENABLED;

    private XRayHttpServerFilterConfigurationProperties server = new XRayHttpServerFilterConfigurationProperties();
    private XRayHttpClientFilterConfigurationProperties client = new XRayHttpClientFilterConfigurationProperties();

    @NonNull
    public XRayHttpServerFilterConfiguration getServer() {
        return server;
    }

    @NonNull
    public XRayHttpClientFilterConfiguration getClient() {
        return client;
    }

    public void setServer(XRayHttpServerFilterConfigurationProperties server) {
        this.server = server;
    }

    public void setClient(XRayHttpClientFilterConfigurationProperties client) {
        this.client = client;
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

}