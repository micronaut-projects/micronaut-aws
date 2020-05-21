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
package io.micronaut.aws.sdk.v2.client.urlConnection;

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.context.annotation.ConfigurationBuilder;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Context;
import software.amazon.awssdk.http.urlconnection.UrlConnectionHttpClient;

/**
 * Configuration properties for the {@link UrlConnectionHttpClient}.
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 2.0.0
 */
@ConfigurationProperties(UrlConnectionClientConfiguration.PREFIX)
@Context
public class UrlConnectionClientConfiguration extends AWSConfiguration {
    public static final String PREFIX = "url-connection-client";

    @ConfigurationBuilder(prefixes = {""}, excludes = {"applyMutation", "tlsKeyManagersProvider", "tlsTrustManagersProvider", "buildWithDefaults"})
    private UrlConnectionHttpClient.Builder builder = UrlConnectionHttpClient.builder();

    /**
     * @return The builder for {@link UrlConnectionHttpClient}
     */
    public UrlConnectionHttpClient.Builder getBuilder() {
        return builder;
    }
}
