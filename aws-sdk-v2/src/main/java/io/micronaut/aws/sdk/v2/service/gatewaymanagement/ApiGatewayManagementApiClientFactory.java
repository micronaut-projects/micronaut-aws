/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.aws.sdk.v2.service.gatewaymanagement;

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
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiAsyncClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiAsyncClientBuilder;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClient;
import software.amazon.awssdk.services.apigatewaymanagementapi.ApiGatewayManagementApiClientBuilder;

/**
 * Factory that creates an Api Gateway client.
 *
 * @author Sergio del Amo
 * @since 3.5.2
 */
@Factory
public class ApiGatewayManagementApiClientFactory extends AwsClientFactory<ApiGatewayManagementApiClientBuilder, ApiGatewayManagementApiAsyncClientBuilder, ApiGatewayManagementApiClient, ApiGatewayManagementApiAsyncClient> {
    /**
     * Constructor.
     *
     * @param credentialsProvider The credentials provider
     * @param regionProvider      The region provider
     * @param userAgentProvider User-Agent Provider
     * @param awsServiceConfiguration  AWS Service Configuration
     */
    protected ApiGatewayManagementApiClientFactory(AwsCredentialsProviderChain credentialsProvider,
                                                   AwsRegionProviderChain regionProvider,
                                                   @Nullable UserAgentProvider userAgentProvider,
                                                   @Nullable @Named(ApiGatewayManagementApiClient.SERVICE_NAME) AWSServiceConfiguration awsServiceConfiguration) {
        super(credentialsProvider, regionProvider, userAgentProvider, awsServiceConfiguration);
    }

    @Override
    protected ApiGatewayManagementApiClientBuilder createSyncBuilder() {
        return ApiGatewayManagementApiClient.builder();
    }

    @Override
    protected ApiGatewayManagementApiAsyncClientBuilder createAsyncBuilder() {
        return ApiGatewayManagementApiAsyncClient.builder();
    }

    @Override
    @Singleton
    public ApiGatewayManagementApiClientBuilder syncBuilder(SdkHttpClient httpClient) {
        return super.syncBuilder(httpClient);
    }

    @Override
    @Bean(preDestroy = "close")
    @Singleton
    public ApiGatewayManagementApiClient syncClient(ApiGatewayManagementApiClientBuilder builder) {
        return super.syncClient(builder);
    }

    @Override
    @Singleton
    @Requires(beans = SdkAsyncHttpClient.class)
    public ApiGatewayManagementApiAsyncClientBuilder asyncBuilder(SdkAsyncHttpClient httpClient) {
        return super.asyncBuilder(httpClient);
    }

    @Override
    @Bean(preDestroy = "close")
    @Singleton
    @Requires(beans = SdkAsyncHttpClient.class)
    public ApiGatewayManagementApiAsyncClient asyncClient(ApiGatewayManagementApiAsyncClientBuilder builder) {
        return super.asyncClient(builder);
    }
}
