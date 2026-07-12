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
package io.micronaut.aws.sdk.v2.client.apache5;

import io.micronaut.aws.sdk.v2.client.urlConnection.UrlConnectionClientFactory;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import software.amazon.awssdk.http.SdkHttpClient;

import jakarta.inject.Singleton;

/**
 * Factory that creates an Apache HTTP client 5.x based {@link SdkHttpClient}. Exactly one AWS SDK
 * sync HTTP client must be on the classpath; the AWS SDK rejects multiple implementations. This
 * factory is active when the {@code apache5-client} is on the classpath, unless the
 * {@code software.amazon.awssdk.http.service.impl} system property selects the URLConnection client.
 *
 * @since 5.1.0
 */
@BootstrapContextCompatible
@Factory
public class Apache5ClientFactory {

    /**
     * @param configuration The Apache 5.x client configuration
     * @return An instance of {@link SdkHttpClient}
     */
    // Deliberately a single bean, unlike the legacy ApacheClientFactory's two methods: this
    // condition already fires when the service-impl property names the apache5 service, so a
    // separate systemPropertyClient method would be redundant (and would double-produce the bean).
    @Bean(preDestroy = "close")
    @Singleton
    @Requires(property = UrlConnectionClientFactory.HTTP_SERVICE_IMPL, notEquals = UrlConnectionClientFactory.URL_CONNECTION_SDK_HTTP_SERVICE)
    public SdkHttpClient apache5Client(Apache5ClientConfiguration configuration) {
        return configuration.getBuilder().build();
    }
}
