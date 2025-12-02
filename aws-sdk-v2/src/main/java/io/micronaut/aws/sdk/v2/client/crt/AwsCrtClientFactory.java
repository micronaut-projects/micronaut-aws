/*
 * Copyright 2017-2025 original authors
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
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Internal;
import jakarta.inject.Singleton;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtAsyncHttpClient;
import software.amazon.awssdk.http.crt.AwsCrtHttpClient;
import software.amazon.awssdk.http.crt.ProxyConfiguration;

import static io.micronaut.aws.sdk.v2.client.netty.NettyClientFactory.ASYNC_SERVICE_IMPL;
import static io.micronaut.aws.sdk.v2.client.urlConnection.UrlConnectionClientFactory.HTTP_SERVICE_IMPL;

/**
 * @since 4.12.0
 */
@Factory
@BootstrapContextCompatible
@Internal
class AwsCrtClientFactory {
    public static final String AWS_CRT_SDK_HTTP_SERVICE = "software.amazon.awssdk.http.crt.AwsCrtSdkHttpService";

    @Prototype
    ProxyConfiguration.Builder createProxyBuilder(AwsCrtClientConfiguration configuration) {
        return ProxyConfiguration.builder();
    }

    @Prototype
    ProxyConfiguration createProxyConfiguration(ProxyConfiguration.Builder builder) {
        return builder.build();
    }

    /**
     *
     * @param proxyConfiguration Proxy Configuration
     * @param awsCrtClientConfiguration AWS CRT Client Configuration
     * @return AWS CRT HTTP Client Builder
     */
    @Prototype
    AwsCrtHttpClient.Builder createAwsCrtHttpClientBuilder(ProxyConfiguration proxyConfiguration,
                                                           AwsCrtClientConfiguration awsCrtClientConfiguration) {
        AwsCrtHttpClient.Builder builder = awsCrtClientConfiguration.getSync();
        if (isProxyConfigured(proxyConfiguration)) {
            builder.proxyConfiguration(proxyConfiguration);
        }
        return builder;
    }

    /**
     * @param awsCrtHttpClientBuilder AWS CRT HTTP Client Builder
     * @return an instance of {@link SdkAsyncHttpClient}
     */
    @Bean(preDestroy = "close")
    @Singleton
    @Requires(property = HTTP_SERVICE_IMPL, value = AWS_CRT_SDK_HTTP_SERVICE)
    public SdkHttpClient systemPropertySdkHttpClient(AwsCrtHttpClient.Builder awsCrtHttpClientBuilder) {
        return awsCrtHttpClientBuilder.build();
    }

    /**
     *
     * @param proxyConfiguration Proxy Configuration
     * @param awsCrtClientConfiguration AWS CRT Client Configuration
     * @return AWS CRT Async HTTP Client Builder
     */
    @Prototype
    AwsCrtAsyncHttpClient.Builder createAwsCrtAsyncHttpClientBuilder(ProxyConfiguration proxyConfiguration,
                                                                     AwsCrtClientConfiguration awsCrtClientConfiguration) {
        AwsCrtAsyncHttpClient.Builder builder = awsCrtClientConfiguration.getAsync();
        if (isProxyConfigured(proxyConfiguration)) {
            builder.proxyConfiguration(proxyConfiguration);
        }
        return builder;
    }

    /**
     * @param awsCrtAsyncHttpClientBuilder AWS CRT Async HTTP Client Builder
     * @return an instance of {@link SdkAsyncHttpClient}
     */
    @Bean(preDestroy = "close")
    @Singleton
    @Requires(property = ASYNC_SERVICE_IMPL, value = AWS_CRT_SDK_HTTP_SERVICE)
    public SdkAsyncHttpClient systemPropertySdkAsyncHttpClient(AwsCrtAsyncHttpClient.Builder awsCrtAsyncHttpClientBuilder) {
        return awsCrtAsyncHttpClientBuilder.build();
    }

    private static boolean isProxyConfigured(ProxyConfiguration proxyConfiguration) {
        return proxyConfiguration.host() != null;
    }
}
