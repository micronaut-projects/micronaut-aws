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
package io.micronaut.aws.sdk.v2.client.urlConnection;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import software.amazon.awssdk.http.SdkHttpClient;

import javax.inject.Singleton;

/**
 * Factory that creates an {@link java.net.URLConnection} based client.
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 2.0.0
 */
@Factory
public class UrlConnectionClientFactory {

    public static final String HTTP_SERVICE_IMPL = "software.amazon.awssdk.http.service.impl";
    public static final String URL_CONNECTION_SDK_HTTP_SERVICE = "software.amazon.awssdk.http.urlconnection.UrlConnectionSdkHttpService";

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
     * Creates an {@link software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient} client if the system property {@value #HTTP_SERVICE_IMPL} is set to
     * {@value #URL_CONNECTION_SDK_HTTP_SERVICE}.
     *
     * @param configuration The URLConnection client configuration
     * @return An instance of {@link SdkHttpClient}
     */
    @Bean(preDestroy = "close")
    @Singleton
    @Requires(property = HTTP_SERVICE_IMPL, value = URL_CONNECTION_SDK_HTTP_SERVICE)
    public SdkHttpClient systemPropertyClient(UrlConnectionClientConfiguration configuration) {
        return doCreateClient(configuration);
    }

    private SdkHttpClient doCreateClient(UrlConnectionClientConfiguration configuration) {
        return configuration.getBuilder().build();
    }

}
