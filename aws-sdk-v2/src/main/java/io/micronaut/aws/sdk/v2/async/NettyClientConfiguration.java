package io.micronaut.aws.sdk.v2.async;

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.aws.sdk.v2.sync.ApacheClientConfiguration;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.ProxyConfiguration;

/**
 * TODO: javadoc
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 1.0.0
 */
@ConfigurationProperties("netty-client")
@BootstrapContextCompatible
public class NettyClientConfiguration extends AWSConfiguration {

    @ConfigurationBuilder(prefixes = {""}, excludes = {"eventLoopGroup", "eventLoopGroupBuilder", "sslProvider", "tlsKeyManagersProvider", "tlsTrustManagersProvider", "proxyConfiguration", "http2Configuration", "buildWithDefaults", "applyMutation"})
    private NettyNioAsyncHttpClient.Builder builder = NettyNioAsyncHttpClient.builder();

    @ConfigurationBuilder(configurationPrefix = "proxy", prefixes = {""}, excludes = {"applyMutation"})
    private ProxyConfiguration.Builder proxy = ProxyConfiguration.builder();

    public NettyNioAsyncHttpClient.Builder getBuilder() {
        return builder.proxyConfiguration(proxy.build());
    }

    public ProxyConfiguration.Builder getProxy() {
        return proxy;
    }
}
