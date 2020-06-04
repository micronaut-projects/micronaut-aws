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
package io.micronaut.aws.sdk.v2.client.apache;

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import software.amazon.awssdk.http.apache.ApacheHttpClient;
import software.amazon.awssdk.http.apache.ProxyConfiguration;

/**
 * Configuration properties for the Apache HTTP client.
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 2.0.0
 */
@ConfigurationProperties(ApacheClientConfiguration.PREFIX)
public class ApacheClientConfiguration extends AWSConfiguration {

    public static final String PREFIX = "apache-client";

    @ConfigurationBuilder(prefixes = {""}, excludes = {"applyMutation", "proxyConfiguration", "httpRoutePlanner", "credentialsProvider", "tlsKeyManagersProvider", "tlsTrustManagersProvider", "buildWithDefaults"})
    private ApacheHttpClient.Builder builder = ApacheHttpClient.builder();

    @ConfigurationBuilder(configurationPrefix = "proxy", prefixes = {""}, excludes = {"applyMutation"})
    private ProxyConfiguration.Builder proxy = ProxyConfiguration.builder();

    /**
     * @return The builder for {@link ApacheHttpClient}
     */
    public ApacheHttpClient.Builder getBuilder() {
        return builder.proxyConfiguration(proxy.build());
    }

    /**
     * @return The builder for {@link ProxyConfiguration}
     */
    public ProxyConfiguration.Builder getProxy() {
        return proxy;
    }
}
