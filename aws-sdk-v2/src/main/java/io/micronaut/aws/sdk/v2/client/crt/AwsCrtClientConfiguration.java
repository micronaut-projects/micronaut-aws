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
package io.micronaut.aws.sdk.v2.client.crt;

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.http.crt.ProxyConfiguration;

/**
 * Configuration properties for the CRT HTTP client.
 *
 * @author Luis Duarte
 * @since 4.12.x
 */
@ConfigurationProperties(AwsCrtClientConfiguration.PREFIX)
@BootstrapContextCompatible
public class AwsCrtClientConfiguration extends AWSConfiguration {

    public static final String PREFIX = "aws-crt-client";

    @ConfigurationBuilder(prefixes = {""}, excludes = {"proxyConfiguration", "buildWithDefaults", "applyMutation"})
    private AwsCrtAsyncHttpClient.Builder builder = AwsCrtAsyncHttpClient.builder();

    @ConfigurationBuilder(configurationPrefix = "proxy", prefixes = {""}, excludes = {"applyMutation", "copy"})
    private ProxyConfiguration.Builder proxy = ProxyConfiguration.builder();

    /**
     * @return The builder for {@link AwsCrtAsyncHttpClient}
     */
    public AwsCrtAsyncHttpClient.Builder getBuilder() {
        return isProxyConfigured() ? builder.proxyConfiguration(proxy.build()) : builder;
    }

    /**
     * @return The builder for {@link ProxyConfiguration}
     */
    public ProxyConfiguration.Builder getProxy() {
        return proxy;
    }

    final boolean isProxyConfigured() {
        ProxyConfiguration proxyConfig = proxy.build();
        return proxyConfig.host() != null;
    }
}
