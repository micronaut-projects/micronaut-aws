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
package io.micronaut.aws.sdk.v2.client.netty;

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.ProxyConfiguration;

/**
 * Configuration properties for the Netty async client.
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 2.0.0
 */
@ConfigurationProperties(NettyClientConfiguration.PREFIX)
@BootstrapContextCompatible
public class NettyClientConfiguration extends AWSConfiguration {

    public static final String PREFIX = "netty-client";

    @ConfigurationBuilder(prefixes = {""}, excludes = {"eventLoopGroup", "eventLoopGroupBuilder", "sslProvider", "tlsKeyManagersProvider", "tlsTrustManagersProvider", "proxyConfiguration", "http2Configuration", "buildWithDefaults", "applyMutation"})
    private NettyNioAsyncHttpClient.Builder builder = NettyNioAsyncHttpClient.builder();

    @ConfigurationBuilder(configurationPrefix = "proxy", prefixes = {""}, excludes = {"applyMutation"})
    private ProxyConfiguration.Builder proxy = ProxyConfiguration.builder();

    /**
     * @return The builder for {@link NettyNioAsyncHttpClient}
     */
    public NettyNioAsyncHttpClient.Builder getBuilder() {
        ProxyConfiguration proxyConfig = proxy.build();
        if (proxyConfig.scheme() == null &&
                proxyConfig.host() == null &&
                proxyConfig.nonProxyHosts().isEmpty()
        ) {
            return builder;
        } else {
            return builder.proxyConfiguration(proxyConfig);
        }
    }

    /**
     * @return The builder for {@link ProxyConfiguration}
     */
    public ProxyConfiguration.Builder getProxy() {
        return proxy;
    }
}
