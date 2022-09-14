package io.micronaut.aws.sdk.v2;

import io.micronaut.aws.sdk.v2.service.AwsClientFactory;
import io.micronaut.aws.sdk.v2.service.AWSServiceConfiguration;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Bean;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.inject.qualifiers.Qualifiers;
import jakarta.inject.Singleton;
import software.amazon.awssdk.auth.credentials.AwsCredentialsProviderChain;
import software.amazon.awssdk.http.SdkHttpClient;
import software.amazon.awssdk.http.async.SdkAsyncHttpClient;
import software.amazon.awssdk.regions.providers.AwsRegionProviderChain;
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClient;
import software.amazon.awssdk.services.rekognition.RekognitionAsyncClientBuilder;
import software.amazon.awssdk.services.rekognition.RekognitionClient;
import software.amazon.awssdk.services.rekognition.RekognitionClientBuilder;

@Requires(property = "spec.name", value = "AwsClientFactorySpec")
//tag::class[]
@Factory
public class RekognitionClientFactory extends AwsClientFactory<RekognitionClientBuilder, RekognitionAsyncClientBuilder, RekognitionClient, RekognitionAsyncClient> {

    protected RekognitionClientFactory(AwsCredentialsProviderChain credentialsProvider, AwsRegionProviderChain regionProvider,
                                       ApplicationContext context) {
        super(credentialsProvider, regionProvider, context.findBean(
            AWSServiceConfiguration.class, Qualifiers.byName(RekognitionClient.SERVICE_NAME)).orElse(null));
    }

    // Sync client
    @Override
    protected RekognitionClientBuilder createSyncBuilder() { // <1>
        return RekognitionClient.builder();
    }

    @Override
    @Singleton
    public RekognitionClientBuilder syncBuilder(SdkHttpClient httpClient) { // <2>
        return super.syncBuilder(httpClient);
    }

    @Override
    @Bean(preDestroy = "close")
    public RekognitionClient syncClient(RekognitionClientBuilder builder) { // <3>
        return super.syncClient(builder);
    }

    // Async client
    @Override
    protected RekognitionAsyncClientBuilder createAsyncBuilder() { // <1>
        return RekognitionAsyncClient.builder();
    }

    @Override
    @Singleton
    @Requires(beans = SdkAsyncHttpClient.class)
    public RekognitionAsyncClientBuilder asyncBuilder(SdkAsyncHttpClient httpClient) { // <2>
        return super.asyncBuilder(httpClient);
    }

    @Override
    @Bean(preDestroy = "close")
    @Requires(beans = SdkAsyncHttpClient.class)
    public RekognitionAsyncClient asyncClient(RekognitionAsyncClientBuilder builder) { // <3>
        return super.asyncClient(builder);
    }
}
//end::class[]
