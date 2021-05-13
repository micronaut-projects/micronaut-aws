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

import io.micronaut.aws.AWSConfiguration;
import software.amazon.awssdk.http.nio.netty.NettyNioAsyncHttpClient;
import software.amazon.awssdk.http.nio.netty.ProxyConfiguration;

/**
 * Configuration properties for the Netty async client.
 * @author Sergio del Amo
 * @since 2.7.0
 */
public interface NettyClientConfiguration {

    String PREFIX = AWSConfiguration.PREFIX + ".netty-client";

    /**
     * @return The builder for {@link NettyNioAsyncHttpClient}
     */
    NettyNioAsyncHttpClient.Builder getBuilder();

    /**
     * @return The builder for {@link ProxyConfiguration}
     */
    ProxyConfiguration.Builder getProxy();
}
