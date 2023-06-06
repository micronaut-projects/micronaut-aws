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
package io.micronaut.aws.sdk.v2.service.servicediscovery;

import io.micronaut.aws.sdk.v2.service.AWSServiceConfiguration;
import io.micronaut.aws.sdk.v2.service.AwsClientFactory;
import io.micronaut.aws.ua.UserAgentProvider;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Named;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.regions.providers.AwsRegionProviderChain;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClient;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryAsyncClientBuilder;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryClient;
import software.amazon.awssdk.services.servicediscovery.ServiceDiscoveryClientBuilder;

/**
 * Factory that creates service discovery clients.
 *
 * @author Denis Stepanov
 */
@Factory
public class ServiceDiscoveryAsyncClientFactory extends AwsClientFactory<ServiceDiscoveryClientBuilder,
        ServiceDiscoveryAsyncClientBuilder, ServiceDiscoveryClient, ServiceDiscoveryAsyncClient> {
    /**
     * Constructor.
     *
     * @param credentialsProvider The credentials provider
     * @param regionProvider      The region provider
     * @param userAgentProvider User-Agent Provider
     * @param awsServiceConfiguration  AWS Service Configuration
     */
    public ServiceDiscoveryAsyncClientFactory(AwsCredentialsProviderChain credentialsProvider,
                                              AwsRegionProviderChain regionProvider,
                                              @Nullable UserAgentProvider userAgentProvider,
                                              @Nullable @Named(ServiceDiscoveryClient.SERVICE_NAME) AWSServiceConfiguration awsServiceConfiguration) {
        super(credentialsProvider, regionProvider, userAgentProvider, awsServiceConfiguration);
    }

    @Override
    protected ServiceDiscoveryClientBuilder createSyncBuilder() {
        return ServiceDiscoveryClient.builder();
    }

    @Override
    protected ServiceDiscoveryAsyncClientBuilder createAsyncBuilder() {
        return ServiceDiscoveryAsyncClient.builder();
    }

    @Requires(missingBeans = ServiceDiscoveryClientBuilder.class)
    @Override
    @Singleton
    public ServiceDiscoveryClientBuilder syncBuilder(SdkHttpClient httpClient) {
        return super.syncBuilder(httpClient);
    }

    @Requires(missingBeans = ServiceDiscoveryClient.class)
    @Override
    @Bean(preDestroy = "close")
    @Singleton
    public ServiceDiscoveryClient syncClient(ServiceDiscoveryClientBuilder builder) {
        return super.syncClient(builder);
    }

    @Requires(missingBeans = ServiceDiscoveryAsyncClientBuilder.class)
    @Override
    @Singleton
    @Requires(beans = SdkAsyncHttpClient.class)
    public ServiceDiscoveryAsyncClientBuilder asyncBuilder(SdkAsyncHttpClient httpClient) {
        return super.asyncBuilder(httpClient);
    }

    @Requires(missingBeans = ServiceDiscoveryAsyncClient.class)
    @Override
    @Bean(preDestroy = "close")
    @Singleton
    @Requires(beans = SdkAsyncHttpClient.class)
    public ServiceDiscoveryAsyncClient asyncClient(ServiceDiscoveryAsyncClientBuilder builder) {
        return super.asyncClient(builder);
    }
}
