package io.micronaut.docs.aws.sdk.v2

import io.micronaut.aws.sdk.v2.service.AWSServiceConfiguration
import io.micronaut.aws.sdk.v2.service.AwsClientFactory
import io.micronaut.aws.ua.UserAgentProvider
import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import io.micronaut.context.annotation.Requires
import jakarta.inject.Named
import jakarta.inject.Singleton
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain
import software.amazon.awssdk.http.SdkHttpClient
import software.amazon.awssdk.http.async.SdkAsyncHttpClient
import software.amazon.awssdk.regions.providers.AwsRegionProviderChain
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClient
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClientBuilder
import software.amazon.awssdk.services.rekognition.RekognitionClient
import software.amazon.awssdk.services.rekognition.RekognitionClientBuilder

@Requires(property = "spec.name", value = "AwsClientFactoryTest")
//tag::class[]
@Factory
class RekognitionClientFactory(
    credentialsProvider: AwsCredentialsProviderChain,
    regionProvider: AwsRegionProviderChain,
    userAgentProvider: UserAgentProvider?,
    @Named(RekognitionClient.SERVICE_NAME) awsServiceConfiguration: AWSServiceConfiguration?
) : AwsClientFactory<RekognitionClientBuilder, RekognitionAsyncClientBuilder, RekognitionClient, RekognitionAsyncClient>(
    credentialsProvider, regionProvider, userAgentProvider, awsServiceConfiguration
) {

    // Sync client
    override fun createSyncBuilder(): RekognitionClientBuilder { // <1>
        return RekognitionClient.builder()
    }

    @Singleton
    override fun syncBuilder(httpClient: SdkHttpClient): RekognitionClientBuilder { // <2>
        return super.syncBuilder(httpClient)
    }

    @Bean(preDestroy = "close")
    override fun syncClient(builder: RekognitionClientBuilder): RekognitionClient { // <3>
        return super.syncClient(builder)
    }

    // Async client
    override fun createAsyncBuilder(): RekognitionAsyncClientBuilder { // <1>
        return RekognitionAsyncClient.builder()
    }

    @Singleton
    @Requires(beans = [SdkAsyncHttpClient::class])
    override fun asyncBuilder(httpClient: SdkAsyncHttpClient): RekognitionAsyncClientBuilder { // <2>
        return super.asyncBuilder(httpClient)
    }

    @Bean(preDestroy = "close")
    @Requires(beans = [SdkAsyncHttpClient::class])
    override fun asyncClient(builder: RekognitionAsyncClientBuilder): RekognitionAsyncClient { // <3>
        return super.asyncClient(builder)
    }
}
//end::class[]
