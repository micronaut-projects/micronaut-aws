/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.aws.sdk.v2.service.s3;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.regions.providers.AwsRegionProviderChain;
import software.amazon.awssdk.services.s3.S3AsyncClient;
import software.amazon.awssdk.services.s3.S3Client;

/**
 * Factory that creates an S3 client.
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 2.0.0
 */
@Factory
public class S3ClientFactory {

    private final AwsCredentialsProviderChain credentialsProvider;
    private final AwsRegionProviderChain regionProvider;
    private final S3ConfigurationProperties configuration;

    public S3ClientFactory(AwsCredentialsProviderChain credentialsProvider, AwsRegionProviderChain regionProvider, S3ConfigurationProperties configuration) {
        this.credentialsProvider = credentialsProvider;
        this.regionProvider = regionProvider;
        this.configuration = configuration;
    }

    /**
     * @param httpClient The sync client
     * @return an {@link S3Client} instance.
     */
    @Bean(preDestroy = "close")
    @BootstrapContextCompatible
    @Requires(beans = SdkHttpClient.class)
    public S3Client s3Client(SdkHttpClient httpClient) {
        Region region = regionProvider.getRegion();
        return S3Client.builder()
                .httpClient(httpClient)
                .region(region)
                .serviceConfiguration(configuration.getBuilder().build())
                .credentialsProvider(credentialsProvider)
                .build();
    }

    /**
     * @param httpClient The async client
     * @return an {@link S3AsyncClient} instance
     */
    @Bean(preDestroy = "close")
    @BootstrapContextCompatible
    @Requires(beans = SdkAsyncHttpClient.class)
    public S3AsyncClient s3AsyncClient(SdkAsyncHttpClient httpClient) {
        Region region = regionProvider.getRegion();
        return S3AsyncClient.builder()
                .httpClient(httpClient)
                .region(region)
                .serviceConfiguration(configuration.getBuilder().build())
                .credentialsProvider(credentialsProvider)
                .build();
    }

}
