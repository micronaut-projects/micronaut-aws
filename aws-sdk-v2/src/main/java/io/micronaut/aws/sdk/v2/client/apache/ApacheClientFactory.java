/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.aws.sdk.v2.client.apache;

import io.micronaut.aws.sdk.v2.client.urlConnection.UrlConnectionClientFactory;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import software.amazon.awssdk.http.SdkHttpClient;

/**
 * Factory that creates an Apache HTTP client.
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 2.0.0
 */
@Factory
public class ApacheClientFactory {

    public static final String APACHE_SDK_HTTP_SERVICE = "software.amazon.awssdk.http.apache.ApacheSdkHttpService";

    /**
     * @param configuration The Apache client configuration
     * @return An instance of {@link SdkHttpClient}
     */
    @Bean(preDestroy = "close")
    @Requires(property = UrlConnectionClientFactory.HTTP_SERVICE_IMPL, notEquals = UrlConnectionClientFactory.URL_CONNECTION_SDK_HTTP_SERVICE)
    public SdkHttpClient apacheClient(ApacheClientConfiguration configuration) {
        return doCreateClient(configuration);
    }

    /**
     * @param configuration The Apache client configuration
     * @return An instance of {@link SdkHttpClient}
     */
    @Bean(preDestroy = "close")
    @Requires(property = UrlConnectionClientFactory.HTTP_SERVICE_IMPL, value = APACHE_SDK_HTTP_SERVICE)
    public SdkHttpClient systemPropertyClient(ApacheClientConfiguration configuration) {
        return doCreateClient(configuration);
    }

    private SdkHttpClient doCreateClient(ApacheClientConfiguration configuration) {
        return configuration.getBuilder().build();
    }
}
