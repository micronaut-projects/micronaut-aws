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

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Prototype;
import io.micronaut.core.annotation.Internal;
import jakarta.inject.Singleton;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtHttpClient;
import software.amazon.awssdk.http.crt.ProxyConfiguration;

/**
 * Factory that creates the synchronous and asynchronous AWS CRT HTTP clients. When the AWS CRT
 * HTTP client is the only implementation of its type on the classpath, this factory provides it
 * as the corresponding AWS SDK HTTP client.
 *
 * @since 5.1.0
 */
@Factory
@BootstrapContextCompatible
@Internal
class AwsCrtClientFactory {
    /**
     * The AWS SDK HTTP service implementation class for the AWS CRT HTTP client.
     */
    public static final String AWS_CRT_SDK_HTTP_SERVICE = "software.amazon.awssdk.http.crt.AwsCrtSdkHttpService";

    /**
     * @param configuration The AWS CRT client configuration
     * @return The configured proxy builder
     */
    @Prototype
    ProxyConfiguration.Builder createProxyConfigurationBuilder(AwsCrtClientConfiguration configuration) {
        return configuration.getProxy();
    }

    /**
     * @param builder The configured proxy builder
     * @return The AWS CRT proxy configuration
     */
    @Singleton
    ProxyConfiguration createProxyConfiguration(ProxyConfiguration.Builder builder) {
        return builder.build();
    }

    /**
     * @param proxyConfiguration The AWS CRT proxy configuration
     * @param configuration The AWS CRT client configuration
     * @return The builder for the asynchronous AWS CRT HTTP client
     */
    @Prototype
    AwsCrtAsyncHttpClient.Builder createAwsCrtAsyncHttpClientBuilder(ProxyConfiguration proxyConfiguration,
                                                                     AwsCrtClientConfiguration configuration) {
        AwsCrtAsyncHttpClient.Builder builder = configuration.getAsync();
        if (isProxyConfigured(proxyConfiguration)) {
            builder.proxyConfiguration(proxyConfiguration);
        }
        return builder;
    }

    /**
     * @param awsCrtAsyncHttpClientBuilder The asynchronous AWS CRT HTTP client builder
     * @return The configured {@link SdkAsyncHttpClient}
     */
    @Bean(preDestroy = "close")
    @Singleton
    public SdkAsyncHttpClient systemPropertySdkAsyncHttpClient(AwsCrtAsyncHttpClient.Builder awsCrtAsyncHttpClientBuilder) {
        return awsCrtAsyncHttpClientBuilder.build();
    }

    /**
     * @param configuration The AWS CRT client configuration
     * @param proxyConfiguration The AWS CRT proxy configuration
     * @return The builder for the synchronous AWS CRT HTTP client
     */
    @Prototype
    AwsCrtHttpClient.Builder createAwsCrtSyncHttpClientBuilder(AwsCrtClientConfiguration configuration,
                                                               ProxyConfiguration proxyConfiguration) {
        AwsCrtHttpClient.Builder builder = configuration.getSync();
        if (isProxyConfigured(proxyConfiguration)) {
            builder.proxyConfiguration(proxyConfiguration);
        }
        return builder;
    }

    /**
     * @param awsCrtHttpClientBuilder The synchronous AWS CRT HTTP client builder
     * @return The configured {@link SdkHttpClient}
     */
    @Bean(preDestroy = "close")
    @Singleton
    public SdkHttpClient systemPropertySdkHttpClient(AwsCrtHttpClient.Builder awsCrtHttpClientBuilder) {
        return awsCrtHttpClientBuilder.build();
    }

    private static boolean isProxyConfigured(ProxyConfiguration proxyConfiguration) {
        return proxyConfiguration.host() != null;
    }
}
