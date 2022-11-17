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
package io.micronaut.aws.sdk.v2.service.cloudwatchlogs;

import io.micronaut.aws.sdk.v2.service.AwsClientFactory;
import io.micronaut.aws.ua.UserAgentProvider;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Inject;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.regions.providers.AwsRegionProviderChain;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsAsyncClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsAsyncClientBuilder;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClientBuilder;

import jakarta.inject.Singleton;

/**
 * Factory that creates a CloudWatch Logs client.
 * @author Nemanja Mikic
 * @since 2.6.0
 */
@Factory
@BootstrapContextCompatible
public class CloudwatchLogsClientFactory extends AwsClientFactory<CloudWatchLogsClientBuilder, CloudWatchLogsAsyncClientBuilder, CloudWatchLogsClient, CloudWatchLogsAsyncClient> {

    /**
     * Constructor.
     *
     * @param credentialsProvider The credentials provider
     * @param regionProvider      The region provider
     * @deprecated Use {@link CloudwatchLogsClientFactory(AwsCredentialsProviderChain,AwsRegionProviderChain,UserAgentProvider)} instead.
     */
    @Deprecated
    protected CloudwatchLogsClientFactory(AwsCredentialsProviderChain credentialsProvider, AwsRegionProviderChain regionProvider) {
        super(credentialsProvider, regionProvider, null);
    }

    /**
     * Constructor.
     *
     * @param credentialsProvider The credentials provider
     * @param regionProvider      The region provider
     * @param userAgentProvider User-Agent Provider
     */
    @Inject
    protected CloudwatchLogsClientFactory(AwsCredentialsProviderChain credentialsProvider,
                                          AwsRegionProviderChain regionProvider,
                                          @Nullable UserAgentProvider userAgentProvider) {
        super(credentialsProvider, regionProvider, userAgentProvider);
    }

    @Override
    protected CloudWatchLogsClientBuilder createSyncBuilder() {
        return CloudWatchLogsClient.builder();
    }

    @Override
    protected CloudWatchLogsAsyncClientBuilder createAsyncBuilder() {
        return CloudWatchLogsAsyncClient.builder();
    }

    @Override
    @Singleton
    public CloudWatchLogsClientBuilder syncBuilder(SdkHttpClient httpClient) {
        return super.syncBuilder(httpClient);
    }

    @Override
    @Bean(preDestroy = "close")
    @Singleton
    public CloudWatchLogsClient syncClient(CloudWatchLogsClientBuilder builder) {
        return super.syncClient(builder);
    }

    @Override
    @Singleton
    @Requires(beans = SdkAsyncHttpClient.class)
    public CloudWatchLogsAsyncClientBuilder asyncBuilder(SdkAsyncHttpClient httpClient) {
        return super.asyncBuilder(httpClient);
    }

    @Override
    @Bean(preDestroy = "close")
    @Singleton
    @Requires(beans = SdkAsyncHttpClient.class)
    public CloudWatchLogsAsyncClient asyncClient(CloudWatchLogsAsyncClientBuilder builder) {
        return super.asyncClient(builder);
    }
}
