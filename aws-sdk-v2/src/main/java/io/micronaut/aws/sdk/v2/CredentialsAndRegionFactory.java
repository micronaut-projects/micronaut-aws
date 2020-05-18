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
 * TODO: javadoc
 *
 * @author Álvaro Sánchez-Mariscal
 * @since 1.0.0
 */
@Factory
public class CredentialsAndRegionFactory {


    @Bean(preDestroy = "close")
    @Singleton
    @BootstrapContextCompatible
    public AwsCredentialsProviderChain awsCredentialsProvider(Environment environment) {
        return AwsCredentialsProviderChain.of(
                EnvironmentAwsCredentialsProvider.create(environment),
                DefaultCredentialsProvider.create()
        );
    }

    @Singleton
    @BootstrapContextCompatible
    public AwsRegionProviderChain awsRegionProvider(Environment environment) {
        return new AwsRegionProviderChain(
                new EnvironmentAwsRegionProvider(environment),
                new DefaultAwsRegionProviderChain()
        );
    }

}
