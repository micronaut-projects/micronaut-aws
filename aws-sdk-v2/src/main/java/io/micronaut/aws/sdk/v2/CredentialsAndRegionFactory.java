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
package io.micronaut.aws.sdk.v2;

import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.env.Environment;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProviderChain;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;

import javax.inject.Singleton;

/**
 * Factory that creates an AWS credentials and region providers that can read values from the Micronaut environment.
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 2.0.0
 */
@Factory
@BootstrapContextCompatible
public class CredentialsAndRegionFactory {

    /**
     * @param environment The {@link Environment}
     * @return An {@link AwsCredentialsProviderChain} that attempts to read the values from the Micronaut environment
     * first, then delegates to {@link DefaultCredentialsProvider}.
     */
    @Bean(preDestroy = "close")
    @Singleton
    public AwsCredentialsProviderChain awsCredentialsProvider(Environment environment) {
        return AwsCredentialsProviderChain.of(
                EnvironmentAwsCredentialsProvider.create(environment),
                DefaultCredentialsProvider.create()
        );
    }

    /**
     * @param environment The {@link Environment}
     * @return An {@link AwsRegionProviderChain} that attempts to read the values from the Micronaut environment
     * first, then delegates to {@link DefaultAwsRegionProviderChain}.
     */
    @Singleton
    public AwsRegionProviderChain awsRegionProvider(Environment environment) {
        return new AwsRegionProviderChain(
                new EnvironmentAwsRegionProvider(environment),
                new DefaultAwsRegionProviderChain()
        );
    }

}
