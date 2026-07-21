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
package io.micronaut.aws.sdk.v2.client.apache5;

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.http.apache5.Apache5HttpClient;
import software.amazon.awssdk.http.apache5.ProxyConfiguration;

/**
 * Configuration properties for the Apache HTTP client 5.x. Bound to the
 * {@code aws.apache-client} prefix, the same prefix as the legacy Apache 4.x client, so that
 * migrating between the two requires no configuration change.
 *
 * @since 5.1.0
 */
@BootstrapContextCompatible
@ConfigurationProperties(Apache5ClientConfiguration.PREFIX)
public class Apache5ClientConfiguration extends AWSConfiguration {

    public static final String PREFIX = "apache-client";

    @ConfigurationBuilder(prefixes = {""}, excludes = {"applyMutation", "proxyConfiguration", "httpRoutePlanner", "credentialsProvider", "tlsKeyManagersProvider", "tlsTrustManagersProvider", "buildWithDefaults", "dnsResolver", "tlsSocketStrategy", "authSchemeRegistry"})
    private final Apache5HttpClient.Builder builder = Apache5HttpClient.builder();

    @ConfigurationBuilder(configurationPrefix = "proxy", prefixes = {""}, excludes = {"applyMutation", "addNonProxyHost"})
    private final ProxyConfiguration.Builder proxy = ProxyConfiguration.builder();

    /**
     * @return The builder for {@link Apache5HttpClient}
     */
    public Apache5HttpClient.Builder getBuilder() {
        return builder.proxyConfiguration(proxy.build());
    }

    /**
     * @return The builder for {@link ProxyConfiguration}
     */
    public ProxyConfiguration.Builder getProxy() {
        return proxy;
    }
}
