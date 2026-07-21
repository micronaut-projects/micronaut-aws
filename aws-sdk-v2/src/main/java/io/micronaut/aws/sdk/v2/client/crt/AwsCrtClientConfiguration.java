/*
 * Copyright 2017-2026 original authors
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
import io.micronaut.core.annotation.NonNull;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtHttpClient;
import software.amazon.awssdk.http.crt.ProxyConfiguration;

/**
 * Configuration properties for the AWS Common Runtime (CRT) HTTP clients. The properties are
 * bound to the {@code aws.crt-client} prefix and configure the synchronous, asynchronous, and
 * proxy client builders.
 *
 * @author Luis Duarte
 * @since 5.1.0
 */
@ConfigurationProperties(AwsCrtClientConfiguration.PREFIX)
@BootstrapContextCompatible
public class AwsCrtClientConfiguration extends AWSConfiguration {

    /**
     * The configuration prefix for the AWS CRT HTTP clients.
     */
    public static final String PREFIX = "crt-client";

    @ConfigurationBuilder(prefixes = {""}, excludes = {"proxyConfiguration", "buildWithDefaults", "applyMutation"})
    private AwsCrtHttpClient.Builder sync = AwsCrtHttpClient.builder();

    @ConfigurationBuilder(prefixes = {""}, excludes = {"proxyConfiguration", "buildWithDefaults", "applyMutation"})
    private AwsCrtAsyncHttpClient.Builder async = AwsCrtAsyncHttpClient.builder();

    @ConfigurationBuilder(configurationPrefix = "proxy", prefixes = {""}, excludes = {"applyMutation", "copy"})
    private ProxyConfiguration.Builder proxy = ProxyConfiguration.builder();

    /**
     * @return The builder for the synchronous {@link AwsCrtHttpClient}
     */
    @NonNull
    public AwsCrtHttpClient.Builder getSync() {
        return sync;
    }

    /**
     * @return The builder for the asynchronous {@link AwsCrtAsyncHttpClient}
     */
    @NonNull
    public AwsCrtAsyncHttpClient.Builder getAsync() {
        return async;
    }

    /**
     * @return The builder for the AWS CRT {@link ProxyConfiguration}
     */
    public ProxyConfiguration.Builder getProxy() {
        return proxy;
    }
}
