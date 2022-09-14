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
package io.micronaut.aws.sdk.v2.service.secretsmanager;

import io.micronaut.aws.sdk.v2.service.AwsClientFactory;
import io.micronaut.aws.sdk.v2.service.AWSServiceConfiguration;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.regions.providers.AwsRegionProviderChain;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClientBuilder;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient;
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClientBuilder;

/**
 * Factory that creates a Secrets Manager client.
 * @author Sergio del Amo
 * @since 2.6.0
 */
@Factory
@BootstrapContextCompatible
public class SecretsManagerClientFactory extends AwsClientFactory<SecretsManagerClientBuilder, SecretsManagerAsyncClientBuilder, SecretsManagerClient, SecretsManagerAsyncClient> {

    /**
     * Constructor.
     *
     * @param credentialsProvider The credentials provider
     * @param regionProvider      The region provider
     * @param context             The application context
     */
    protected SecretsManagerClientFactory(AwsCredentialsProviderChain credentialsProvider, AwsRegionProviderChain regionProvider, ApplicationContext context) {
        super(credentialsProvider, regionProvider, context.findBean(
            AWSServiceConfiguration.class, Qualifiers.byName(SecretsManagerClient.SERVICE_NAME)).orElse(null));
    }

    @Override
    protected SecretsManagerClientBuilder createSyncBuilder() {
        return SecretsManagerClient.builder();
    }

    @Override
    protected SecretsManagerAsyncClientBuilder createAsyncBuilder() {
        return SecretsManagerAsyncClient.builder();
    }

    @Override
    @Singleton
    public SecretsManagerClientBuilder syncBuilder(SdkHttpClient httpClient) {
        return super.syncBuilder(httpClient);
    }

    @Override
    @Bean(preDestroy = "close")
    @Singleton
    public SecretsManagerClient syncClient(SecretsManagerClientBuilder builder) {
        return super.syncClient(builder);
    }

    @Override
    @Singleton
    @Requires(beans = SdkAsyncHttpClient.class)
    public SecretsManagerAsyncClientBuilder asyncBuilder(SdkAsyncHttpClient httpClient) {
        return super.asyncBuilder(httpClient);
    }

    @Override
    @Bean(preDestroy = "close")
    @Singleton
    @Requires(beans = SdkAsyncHttpClient.class)
    public SecretsManagerAsyncClient asyncClient(SecretsManagerAsyncClientBuilder builder) {
        return super.asyncClient(builder);
    }
}
