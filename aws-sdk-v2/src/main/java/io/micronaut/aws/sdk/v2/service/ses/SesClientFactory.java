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
package io.micronaut.aws.sdk.v2.service.ses;

import io.micronaut.aws.sdk.v2.service.AWSServiceConfiguration;
import io.micronaut.aws.sdk.v2.service.AwsClientFactory;
import io.micronaut.aws.ua.UserAgentProvider;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.regions.providers.AwsRegionProviderChain;
import software.amazon.awssdk.services.ses.SesAsyncClient;
import software.amazon.awssdk.services.ses.SesAsyncClientBuilder;
import software.amazon.awssdk.services.ses.SesClient;
import software.amazon.awssdk.services.ses.SesClientBuilder;

/**
 * Factory that creates a SES client.
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 2.0.0
 */
@Factory
public class SesClientFactory extends AwsClientFactory<SesClientBuilder, SesAsyncClientBuilder, SesClient, SesAsyncClient> {
    /**
     * Constructor.
     *
     * @param credentialsProvider The credentials provider
     * @param regionProvider      The region provider
     * @deprecated Use {@link SesClientFactory(AwsCredentialsProviderChain,AwsRegionProviderChain,UserAgentProvider,AWSServiceConfiguration)} instead.
     */
    @Deprecated
    protected SesClientFactory(AwsCredentialsProviderChain credentialsProvider, AwsRegionProviderChain regionProvider) {
        super(credentialsProvider, regionProvider, null);
    }

    /**
     * Constructor.
     *
     * @param credentialsProvider The credentials provider
     * @param regionProvider      The region provider
     * @param userAgentProvider User-Agent Provider
     * @deprecated Use {@link SesClientFactory(AwsCredentialsProviderChain,AwsRegionProviderChain,UserAgentProvider,AWSServiceConfiguration)} instead.
     */
    @Deprecated
    protected SesClientFactory(AwsCredentialsProviderChain credentialsProvider,
                               AwsRegionProviderChain regionProvider,
                               @Nullable UserAgentProvider userAgentProvider) {
        super(credentialsProvider, regionProvider, userAgentProvider);
    }

    /**
     * Constructor.
     *
     * @param credentialsProvider The credentials provider
     * @param regionProvider      The region provider
     * @param userAgentProvider User-Agent Provider
     * @param awsServiceConfiguration  AWS Service Configuration
     */
    @Inject
    protected SesClientFactory(AwsCredentialsProviderChain credentialsProvider,
                               AwsRegionProviderChain regionProvider,
                               @Nullable UserAgentProvider userAgentProvider,
                               @Nullable @Named(SesClient.SERVICE_NAME) AWSServiceConfiguration awsServiceConfiguration) {
        super(credentialsProvider, regionProvider, userAgentProvider, awsServiceConfiguration);
    }

    @Override
    protected SesClientBuilder createSyncBuilder() {
        return SesClient.builder();
    }

    @Override
    protected SesAsyncClientBuilder createAsyncBuilder() {
        return SesAsyncClient.builder();
    }

    @Override
    @Singleton
    public SesClientBuilder syncBuilder(SdkHttpClient httpClient) {
        return super.syncBuilder(httpClient);
    }

    @Override
    @Bean(preDestroy = "close")
    @Singleton
    public SesClient syncClient(SesClientBuilder builder) {
        return super.syncClient(builder);
    }

    @Override
    @Singleton
    @Requires(beans = SdkAsyncHttpClient.class)
    public SesAsyncClientBuilder asyncBuilder(SdkAsyncHttpClient httpClient) {
        return super.asyncBuilder(httpClient);
    }

    @Override
    @Bean(preDestroy = "close")
    @Singleton
    @Requires(beans = SdkAsyncHttpClient.class)
    public SesAsyncClient asyncClient(SesAsyncClientBuilder builder) {
        return super.asyncClient(builder);
    }
}
