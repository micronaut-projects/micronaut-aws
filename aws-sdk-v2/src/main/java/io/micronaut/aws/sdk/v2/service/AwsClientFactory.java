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

import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.awscore.client.builder.AwsAsyncClientBuilder;
import software.amazon.awssdk.awscore.client.builder.AwsClientBuilder;
import software.amazon.awssdk.awscore.client.builder.AwsSyncClientBuilder;
import software.amazon.awssdk.core.SdkClient;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.regions.providers.AwsRegionProviderChain;

import java.util.Optional;

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

    protected final AwsCredentialsProviderChain credentialsProvider;
    protected final AwsRegionProviderChain regionProvider;
    protected final ServiceClientConfiguration configuration;

    /**
     * Constructor.
     *
     * @param credentialsProvider The credentials provider
     * @param regionProvider The region provider
     * @param configuration The service configuration
     */
    protected AwsClientFactory(AwsCredentialsProviderChain credentialsProvider, AwsRegionProviderChain regionProvider,
                               ServiceClientConfiguration configuration) {
        this.credentialsProvider = credentialsProvider;
        this.regionProvider = regionProvider;
        this.configuration = configuration;
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
        SB sb = createSyncBuilder()
                .httpClient(httpClient)
                .region(regionProvider.getRegion())
                .credentialsProvider(credentialsProvider);
        Optional.ofNullable(configuration.getEndpointOverride()).ifPresent(sb::endpointOverride);
        return sb;
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
        AB ab = createAsyncBuilder()
                .httpClient(httpClient)
                .region(regionProvider.getRegion())
                .credentialsProvider(credentialsProvider);
        Optional.ofNullable(configuration.getEndpointOverride()).ifPresent(ab::endpointOverride);
        return ab;
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
