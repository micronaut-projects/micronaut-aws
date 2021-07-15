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
package io.micronaut.aws.sdk.v2.service.dynamodb;

import io.micronaut.aws.sdk.v2.service.AwsClientFactory;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.regions.providers.AwsRegionProviderChain;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbAsyncClientBuilder;
import software.amazon.awssdk.services.dynamodb.DynamoDbClient;
import software.amazon.awssdk.services.dynamodb.DynamoDbClientBuilder;

import jakarta.inject.Singleton;

/**
 * Factory that creates a Dynamo DB client.
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 2.0.0
 */
@Factory
public class DynamoDbClientFactory extends AwsClientFactory<DynamoDbClientBuilder, DynamoDbAsyncClientBuilder, DynamoDbClient, DynamoDbAsyncClient> {

    /**
     * Constructor.
     *
     * @param credentialsProvider The credentials provider
     * @param regionProvider      The region provider
     */
    protected DynamoDbClientFactory(AwsCredentialsProviderChain credentialsProvider, AwsRegionProviderChain regionProvider) {
        super(credentialsProvider, regionProvider);
    }

    @Override
    protected DynamoDbClientBuilder createSyncBuilder() {
        return DynamoDbClient.builder();
    }

    @Override
    protected DynamoDbAsyncClientBuilder createAsyncBuilder() {
        return DynamoDbAsyncClient.builder();
    }

    @Override
    @Singleton
    public DynamoDbClientBuilder syncBuilder(SdkHttpClient httpClient) {
        return super.syncBuilder(httpClient);
    }

    @Override
    @Bean(preDestroy = "close")
    @Singleton
    public DynamoDbClient syncClient(DynamoDbClientBuilder builder) {
        return super.syncClient(builder);
    }

    @Override
    @Singleton
    @Requires(beans = SdkAsyncHttpClient.class)
    public DynamoDbAsyncClientBuilder asyncBuilder(SdkAsyncHttpClient httpClient) {
        return super.asyncBuilder(httpClient);
    }

    @Override
    @Bean(preDestroy = "close")
    @Singleton
    @Requires(beans = SdkAsyncHttpClient.class)
    public DynamoDbAsyncClient asyncClient(DynamoDbAsyncClientBuilder builder) {
        return super.asyncClient(builder);
    }
}
