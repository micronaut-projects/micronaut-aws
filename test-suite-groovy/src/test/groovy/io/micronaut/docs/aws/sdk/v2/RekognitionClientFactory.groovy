package io.micronaut.docs.aws.sdk.v2

import io.micronaut.aws.sdk.v2.service.AWSServiceConfiguration
import io.micronaut.aws.sdk.v2.service.AwsClientFactory
import io.micronaut.aws.ua.UserAgentProvider
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import jakarta.inject.Named
import jakarta.inject.Singleton
import org.jspecify.annotations.Nullable
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.async.SdkAsyncHttpClient
import software.amazon.awssdk.regions.providers.AwsRegionProviderChain
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClient
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClientBuilder
import software.amazon.awssdk.services.rekognition.RekognitionClient
import software.amazon.awssdk.services.rekognition.RekognitionClientBuilder

@Requires(property = "spec.name", value = "AwsClientFactorySpec")
//tag::class[]
@Factory
class RekognitionClientFactory extends AwsClientFactory<RekognitionClientBuilder, RekognitionAsyncClientBuilder, RekognitionClient, RekognitionAsyncClient> {
    /**
     *
     * @param credentialsProvider The credentials provider
     * @param regionProvider      The region provider
     * @param userAgentProvider User-Agent provider
     * @param awsServiceConfiguration  AWS Service Configuration
     */
    protected RekognitionClientFactory(AwsCredentialsProviderChain credentialsProvider,
                                       AwsRegionProviderChain regionProvider,
                                       @Nullable UserAgentProvider userAgentProvider,
                                       @Nullable @Named(RekognitionClient.SERVICE_NAME) AWSServiceConfiguration awsServiceConfiguration) {
        super(credentialsProvider, regionProvider, userAgentProvider, awsServiceConfiguration)
    }

    // Sync client
    @Override
    protected RekognitionClientBuilder createSyncBuilder() { // <1>
        RekognitionClient.builder()
    }

    @Override
    @Singleton
    RekognitionClientBuilder syncBuilder(SdkHttpClient httpClient) { // <2>
        super.syncBuilder(httpClient)
    }

    @Override
    @Bean(preDestroy = "close")
    RekognitionClient syncClient(RekognitionClientBuilder builder) { // <3>
        super.syncClient(builder)
    }

    // Async client
    @Override
    protected RekognitionAsyncClientBuilder createAsyncBuilder() { // <1>
        RekognitionAsyncClient.builder()
    }

    @Override
    @Singleton
    @Requires(beans = SdkAsyncHttpClient)
    RekognitionAsyncClientBuilder asyncBuilder(SdkAsyncHttpClient httpClient) { // <2>
        super.asyncBuilder(httpClient)
    }

    @Override
    @Bean(preDestroy = "close")
    @Requires(beans = SdkAsyncHttpClient)
    RekognitionAsyncClient asyncClient(RekognitionAsyncClientBuilder builder) { // <3>
        super.asyncClient(builder)
    }
}
//end::class[]
