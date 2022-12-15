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
package io.micronaut.aws.sdk.v2.service.s3;

import io.micronaut.aws.sdk.v2.service.AwsClientFactory;
import io.micronaut.aws.ua.UserAgentProvider;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.regions.providers.AwsRegionProviderChain;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3AsyncClientBuilder;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.S3ClientBuilder;

import jakarta.inject.Singleton;

/**
 * Factory that creates an S3 client.
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 2.0.0
 */
@Factory
public class S3ClientFactory extends AwsClientFactory<S3ClientBuilder, S3AsyncClientBuilder, S3Client, S3AsyncClient> {

    private final S3ConfigurationProperties configuration;

    /**
     * Constructor.
     *
     * @param credentialsProvider The credentials provider
     * @param regionProvider The region provider
     * @param configuration The service configuration
     * @param userAgentProvider User-Agent Provider
     */
    public S3ClientFactory(AwsCredentialsProviderChain credentialsProvider,
                           AwsRegionProviderChain regionProvider,
                           S3ConfigurationProperties configuration,
                           @Nullable UserAgentProvider userAgentProvider) {
        super(credentialsProvider, regionProvider, userAgentProvider);
        this.configuration = configuration;
    }

    @Override
    protected S3ClientBuilder createSyncBuilder() {
       S3ClientBuilder builder = S3Client.builder();
       if (configuration.getEndpointOverride() != null) {
           builder.endpointOverride(configuration.getEndpointOverride());
       }
       builder.serviceConfiguration(configuration.getBuilder().build());
        return builder;
    }

    @Override
    protected S3AsyncClientBuilder createAsyncBuilder() {
        S3AsyncClientBuilder builder = S3AsyncClient.builder();
        if (configuration.getEndpointOverride() != null) {
            builder.endpointOverride(configuration.getEndpointOverride());
        }
        builder.serviceConfiguration(configuration.getBuilder().build());
        return builder;
    }

    @Override
    @Singleton
    public S3ClientBuilder syncBuilder(SdkHttpClient httpClient) {
        return super.syncBuilder(httpClient);
    }

    @Override
    @Bean(preDestroy = "close")
    @Singleton
    public S3Client syncClient(S3ClientBuilder builder) {
        return super.syncClient(builder);
    }

    @Override
    @Singleton
    @Requires(beans = SdkAsyncHttpClient.class)
    public S3AsyncClientBuilder asyncBuilder(SdkAsyncHttpClient httpClient) {
        return super.asyncBuilder(httpClient);
    }

    @Override
    @Bean(preDestroy = "close")
    @Singleton
    @Requires(beans = SdkAsyncHttpClient.class)
    public S3AsyncClient asyncClient(S3AsyncClientBuilder builder) {
        return super.asyncClient(builder);
    }
}
