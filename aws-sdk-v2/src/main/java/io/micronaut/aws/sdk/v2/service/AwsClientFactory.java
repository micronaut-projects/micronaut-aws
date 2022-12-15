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
package io.micronaut.aws.sdk.v2.service;

import io.micronaut.aws.ua.UserAgentProvider;
import io.micronaut.core.annotation.Nullable;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.awscore.client.builder.AwsAsyncClientBuilder;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.awscore.client.builder.AwsSyncClientBuilder;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.core.client.config.SdkAdvancedClientOption;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.regions.providers.AwsRegionProviderChain;

/**
 * Abstract class that eases creation of AWS client factories.
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 2.0.0
 *
 * @param <SB> The sync builder
 * @param <AB> The async builder
 * @param <SC> The sync client
 * @param <AC> The async client
 */
public abstract class AwsClientFactory<SB extends AwsSyncClientBuilder<SB, SC> & AwsClientBuilder<SB, SC>, AB extends AwsAsyncClientBuilder<AB, AC> & AwsClientBuilder<AB, AC>, SC, AC extends SdkClient> {
    private static final Logger LOG = LoggerFactory.getLogger(AwsClientFactory.class);

    protected final AwsCredentialsProviderChain credentialsProvider;
    protected final AwsRegionProviderChain regionProvider;

    @Nullable
    protected final UserAgentProvider userAgentProvider;

    /**
     * Constructor.
     *
     * @param credentialsProvider The credentials provider
     * @param regionProvider The region provider
     * @param userAgentProvider User-Agent Provider
     */
    protected AwsClientFactory(AwsCredentialsProviderChain credentialsProvider,
                               AwsRegionProviderChain regionProvider,
                               @Nullable UserAgentProvider userAgentProvider) {
        this.credentialsProvider = credentialsProvider;
        this.regionProvider = regionProvider;
        this.userAgentProvider = userAgentProvider;
    }

    /**
     * Configures the builder so that it uses the appropriate HTTP client, credentials and region providers.
     *
     * Subclasses may want to override this method and annotate it with {@code @Singleton}.
     *
     * @param httpClient The sync HTTP client
     * @return The sync builder
     */
    public SB syncBuilder(SdkHttpClient httpClient) {
        return createSyncBuilder()
            .httpClient(httpClient)
            .region(regionProvider.getRegion())
            .credentialsProvider(credentialsProvider)
            .overrideConfiguration(conf -> {
                if (userAgentProvider != null) {
                    String ua = userAgentProvider.userAgent();
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Setting User-Agent for AWS SDK to {}", ua);
                    }
                    conf.putAdvancedOption(SdkAdvancedClientOption.USER_AGENT_SUFFIX, ua);
                }
            });
    }

    /**
     * Creates the sync client. It requires a bean of type {@code SB}.
     *
     * @param builder The sync builder
     * @return The sync AWS client
     * @see #syncBuilder(SdkHttpClient)
     */
    public SC syncClient(SB builder) {
        return builder.build();
    }

    /**
     * Configures the builder so that it uses the appropriate HTTP client, credentials and region providers.
     *
     * Subclasses may want to override this method and annotate it with {@code @Singleton}.
     *
     * @param httpClient The async HTTP client
     * @return The async builder
     */
    public AB asyncBuilder(SdkAsyncHttpClient httpClient) {
        return createAsyncBuilder()
            .httpClient(httpClient)
            .region(regionProvider.getRegion())
            .credentialsProvider(credentialsProvider)
            .overrideConfiguration(conf -> {
                if (userAgentProvider != null) {
                    String ua = userAgentProvider.userAgent();
                    if (LOG.isTraceEnabled()) {
                        LOG.trace("Setting User-Agent for AWS SDK to {}", ua);
                    }
                    conf.putAdvancedOption(SdkAdvancedClientOption.USER_AGENT_SUFFIX, ua);
                }
            });
    }

    /**
     * Creates the async client. It requires a bean of type {@code AB}.
     *
     * @param builder The async builder
     * @return The async AWS client
     */
    public AC asyncClient(AB builder) {
        return builder.build();
    }

    /**
     * Implementations need to create the builder, eg: {@code S3Client.builder();}.
     *
     * @return The sync builder
     */
    protected abstract SB createSyncBuilder();

    /**
     * Implementations need to create the builder, eg: {@code S3AsyncClient.builder();}.
     *
     * @return The async builder
     */
    protected abstract AB createAsyncBuilder();
}
