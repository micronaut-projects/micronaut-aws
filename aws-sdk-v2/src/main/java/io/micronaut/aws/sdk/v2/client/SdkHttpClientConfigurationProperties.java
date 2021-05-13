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
package io.micronaut.aws.sdk.v2.client;

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * {@link ConfigurationProperties} implementation of {@link SdkHttpClientConfiguration}.
 * @author Sergio del Amo
 * @since 2.7.0
 */
@ConfigurationProperties(SdkHttpClientConfigurationProperties.PREFIX)
public class SdkHttpClientConfigurationProperties implements SdkHttpClientConfiguration {
    public static final String PREFIX = AWSConfiguration.PREFIX + ".sdk-http-client";

    private static final boolean DEFAULT_BOOTSTRAP = false;
    private boolean bootstrap = DEFAULT_BOOTSTRAP;

    @Override
    public boolean getBootstrap() {
        return false;
    }

    /**
     * Whether the sdk http client should be loaded into the Bootstrap Context. Default value ({@value #DEFAULT_BOOTSTRAP}).
     * @param bootstrap Whether the sdk http client should be loaded into the Bootstrap Context.
     */
    public void setBootstrap(boolean bootstrap) {
        this.bootstrap = bootstrap;
    }
}
