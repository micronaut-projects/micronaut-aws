package io.micronaut.aws.sdk.v2.sync;

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.apache.ProxyConfiguration;

/**
 * TODO: javadoc
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 1.0.0
 */
@ConfigurationProperties("apache-client")
@BootstrapContextCompatible
public class ApacheClientConfiguration extends AWSConfiguration {

    @ConfigurationBuilder(prefixes = {""}, excludes = {"applyMutation", "proxyConfiguration", "httpRoutePlanner", "credentialsProvider", "tlsKeyManagersProvider", "tlsTrustManagersProvider", "buildWithDefaults"})
    private ApacheHttpClient.Builder builder = ApacheHttpClient.builder();

    @ConfigurationBuilder(configurationPrefix = "proxy", prefixes = {""}, excludes = {"applyMutation"})
    private ProxyConfiguration.Builder proxy = ProxyConfiguration.builder();

    public ApacheHttpClient.Builder getBuilder() {
        return builder.proxyConfiguration(proxy.build());
    }

    public ProxyConfiguration.Builder getProxy() {
        return proxy;
    }
}
