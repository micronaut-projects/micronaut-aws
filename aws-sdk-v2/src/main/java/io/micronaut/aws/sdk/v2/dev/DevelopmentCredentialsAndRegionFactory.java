/*
 * Copyright 2017-2026 original authors
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
package io.micronaut.aws.sdk.v2.dev;

import io.micronaut.aws.sdk.v2.CredentialsAndRegionFactory;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.context.env.DevelopmentActive;
import io.micronaut.core.annotation.Internal;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.regions.providers.AwsRegionProviderChain;
import software.amazon.awssdk.regions.providers.DefaultAwsRegionProviderChain;

/**
 * Replaces, in development mode only, the credentials and region provider chains of
 * {@link CredentialsAndRegionFactory} with chains that resolve the same values in the same order, first from the
 * {@code aws} configuration, then from the defaults of the SDK, but hold a copy of that configuration rather than the
 * environment. A chain holding the environment is bound to its context, and the context refuses to retain any client
 * built on it, so that without this every AWS SDK client would be made again on each restart.
 *
 * @author graemerocher
 * @since 5.2.0
 */
@Internal
@Factory
@DevelopmentActive
@BootstrapContextCompatible
final class DevelopmentCredentialsAndRegionFactory {

    /**
     * @param environment The {@code aws} configuration copied from the environment
     * @return A chain that resolves the credentials from the copied configuration first, then from
     * {@link DefaultCredentialsProvider}
     */
    @Bean(preDestroy = "close")
    @Singleton
    @Replaces(bean = AwsCredentialsProviderChain.class, factory = CredentialsAndRegionFactory.class)
    AwsCredentialsProviderChain awsCredentialsProvider(AwsEnvironmentSnapshot environment) {
        return AwsCredentialsProviderChain.of(
            new CopiedAwsCredentialsProvider(environment),
            DefaultCredentialsProvider.create()
        );
    }

    /**
     * @param environment The {@code aws} configuration copied from the environment
     * @return A chain that resolves the region from the copied configuration first, then from
     * {@link DefaultAwsRegionProviderChain}
     */
    @Singleton
    @Replaces(bean = AwsRegionProviderChain.class, factory = CredentialsAndRegionFactory.class)
    AwsRegionProviderChain awsRegionProvider(AwsEnvironmentSnapshot environment) {
        return new AwsRegionProviderChain(
            new CopiedAwsRegionProvider(environment),
            new DefaultAwsRegionProviderChain()
        );
    }
}
