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