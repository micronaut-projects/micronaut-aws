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
package io.micronaut.aws.sdk.v2.client.urlConnection;

import io.micronaut.aws.sdk.v2.client.SdkHttpClientConfigurationProperties;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import software.amazon.awssdk.http.SdkHttpClient;

import javax.inject.Singleton;

/**
 * A {@link BootstrapContextCompatible} factory that creates an {@link java.net.URLConnection} based client.
 *
 * @author Sergio del Amo
 * @since 2.7.0
 */
@BootstrapContextCompatible
@Requires(property = SdkHttpClientConfigurationProperties.PREFIX + ".bootstrap", value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
@Factory
public class BootstrapUrlConnectionClientFactory {

    /**
     * Creates an {@link software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient} client if there are no other clients configured.
     *
     * @param configuration The URLConnection client configuration
     * @return An instance of {@link SdkHttpClient}
     */
    @Bean(preDestroy = "close")
    @Singleton
    @Requires(missingBeans = SdkHttpClient.class)
    public SdkHttpClient urlConnectionClient(UrlConnectionClientConfiguration configuration) {
        return doCreateClient(configuration);
    }

    /**
     * Creates an {@link software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient} client if the system property {@value UrlConnectionClientFactory#HTTP_SERVICE_IMPL} is set to
     * {@value UrlConnectionClientFactory#URL_CONNECTION_SDK_HTTP_SERVICE}.
     *
     * @param configuration The URLConnection client configuration
     * @return An instance of {@link SdkHttpClient}
     */
    @Bean(preDestroy = "close")
    @Singleton
    @Requires(property = UrlConnectionClientFactory.HTTP_SERVICE_IMPL, value = UrlConnectionClientFactory.URL_CONNECTION_SDK_HTTP_SERVICE)
    public SdkHttpClient systemPropertyClient(UrlConnectionClientConfiguration configuration) {
        return doCreateClient(configuration);
    }

    private SdkHttpClient doCreateClient(UrlConnectionClientConfiguration configuration) {
        return configuration.getBuilder().build();
    }
}
