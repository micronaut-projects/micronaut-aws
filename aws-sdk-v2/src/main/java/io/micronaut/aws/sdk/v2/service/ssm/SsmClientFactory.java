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
package io.micronaut.aws.sdk.v2.service.ssm;

import io.micronaut.aws.sdk.v2.service.AWSServiceConfiguration;
import io.micronaut.aws.sdk.v2.service.AwsClientFactory;
import io.micronaut.aws.ua.UserAgentProvider;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.regions.providers.AwsRegionProviderChain;
import software.amazon.awssdk.services.ssm.SsmAsyncClient;
import software.amazon.awssdk.services.ssm.SsmAsyncClientBuilder;
import software.amazon.awssdk.services.ssm.SsmClient;
import software.amazon.awssdk.services.ssm.SsmClientBuilder;

/**
 * Factory that creates a SSM client.
 *
 * @author ttzn
 * @since 2.3.0
 */
@Factory
@BootstrapContextCompatible
public class SsmClientFactory extends AwsClientFactory<SsmClientBuilder, SsmAsyncClientBuilder, SsmClient, SsmAsyncClient> {
    /**
     * Constructor.
     *
     * @param credentialsProvider The credentials provider
     * @param regionProvider      The region provider
     * @param userAgentProvider User-Agent Provider
     * @param awsServiceConfiguration AWS Service Configuration
     */
    protected SsmClientFactory(AwsCredentialsProviderChain credentialsProvider,
                               AwsRegionProviderChain regionProvider,
                               @Nullable UserAgentProvider userAgentProvider,
                               @Nullable @Named(SsmClient.SERVICE_NAME) AWSServiceConfiguration awsServiceConfiguration) {
        super(credentialsProvider, regionProvider, userAgentProvider, awsServiceConfiguration);
    }

    @Override
    protected SsmClientBuilder createSyncBuilder() {
        return SsmClient.builder();
    }

    @Override
    protected SsmAsyncClientBuilder createAsyncBuilder() {
        return SsmAsyncClient.builder();
    }

    @Override
    @Singleton
    @BootstrapContextCompatible
    public SsmClientBuilder syncBuilder(SdkHttpClient httpClient) {
        return super.syncBuilder(httpClient);
    }

    @Override
    @Bean(preDestroy = "close")
    @Singleton
    @BootstrapContextCompatible
    public SsmClient syncClient(SsmClientBuilder builder) {
        return super.syncClient(builder);
    }

    @Override
    @Singleton
    @Requires(beans = SdkAsyncHttpClient.class)
    @BootstrapContextCompatible
    public SsmAsyncClientBuilder asyncBuilder(SdkAsyncHttpClient httpClient) {
        return super.asyncBuilder(httpClient);
    }

    @Override
    @Bean(preDestroy = "close")
    @Singleton
    @Requires(beans = SdkAsyncHttpClient.class)
    @BootstrapContextCompatible
    public SsmAsyncClient asyncClient(SsmAsyncClientBuilder builder) {
        return super.asyncClient(builder);
    }
}
