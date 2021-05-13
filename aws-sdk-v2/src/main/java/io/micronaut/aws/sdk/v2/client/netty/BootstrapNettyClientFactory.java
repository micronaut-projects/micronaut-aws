/*
 * Copyright 2017-2021 original authors
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

import io.micronaut.aws.sdk.v2.client.SdkHttpClientConfigurationProperties;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;

import javax.inject.Singleton;

/**
 * A {@link BootstrapContextCompatible} Factory that creates a Netty client.
 *
 * @author Sergio del Amo
 * @since 2.7.0
 */
@BootstrapContextCompatible
@Factory
@Requires(property = SdkHttpClientConfigurationProperties.PREFIX + ".bootstrap", value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
public class BootstrapNettyClientFactory {
    /**
     * @param configuration The Netty client configuration
     * @return an instance of {@link SdkAsyncHttpClient}
     */
    @Bean(preDestroy = "close")
    @Singleton
    public SdkAsyncHttpClient nettyClient(NettyClientConfiguration configuration) {
        return doCreateClient(configuration);
    }

    /**
     * @param configuration The Netty client configuration
     * @return an instance of {@link SdkAsyncHttpClient}
     */
    @Bean(preDestroy = "close")
    @Singleton
    @Requires(property = NettyClientFactory.ASYNC_SERVICE_IMPL, value = NettyClientFactory.NETTY_SDK_ASYNC_HTTP_SERVICE)
    public SdkAsyncHttpClient systemPropertyClient(NettyClientConfiguration configuration) {
        return doCreateClient(configuration);
    }

    private SdkAsyncHttpClient doCreateClient(NettyClientConfiguration configuration) {
        return configuration.getBuilder().build();
    }
}
