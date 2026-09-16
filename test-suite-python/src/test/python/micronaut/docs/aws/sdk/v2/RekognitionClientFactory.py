from typing import Annotated

import java
from jakarta.inject import Named, Singleton
from micronaut.aws.sdk.v2.service import AWSServiceConfiguration
from micronaut.aws.ua import UserAgentProvider
from micronaut.context.annotation import Bean, Factory, Requires
from software.amazon.awssdk.auth.credentials import AwsCredentialsProviderChain
from software.amazon.awssdk.core.client.config import ClientOverrideConfiguration, SdkAdvancedClientOption
from software.amazon.awssdk.http import SdkHttpClient
from software.amazon.awssdk.regions.providers import AwsRegionProviderChain
from software.amazon.awssdk.services.rekognition import (
    RekognitionAsyncClient,
    RekognitionAsyncClientBuilder,
    RekognitionClient,
    RekognitionClientBuilder,
)

# TODO(python): java.type needed because `async` is a Python keyword, so the package software.amazon.awssdk.http.async
# cannot be imported; see DISABLED_TESTS.md
SdkAsyncHttpClient = java.type("software.amazon.awssdk.http.async.SdkAsyncHttpClient")


@Requires(property="spec.name", value="AwsClientFactoryTest")
# tag::class[]
@Factory
class RekognitionClientFactory:

    def __init__(self,
                 credentials_provider: AwsCredentialsProviderChain,
                 region_provider: AwsRegionProviderChain,
                 user_agent_provider: UserAgentProvider | None,
                 aws_service_configuration: Annotated[AWSServiceConfiguration | None, Named("rekognition")]):
        self.credentials_provider = credentials_provider
        self.region_provider = region_provider
        self.user_agent_provider = user_agent_provider
        self.aws_service_configuration = aws_service_configuration

    # Sync client
    @Singleton
    def sync_builder(self, http_client: SdkHttpClient) -> RekognitionClientBuilder:  # <2>
        return self.configure(RekognitionClient.builder().httpClient(http_client))  # <1>

    @Bean(preDestroy="close")
    def sync_client(self, builder: RekognitionClientBuilder) -> RekognitionClient:  # <3>
        return builder.build()

    # Async client
    @Singleton
    @Requires(beans=SdkAsyncHttpClient)
    def async_builder(self, http_client: SdkAsyncHttpClient) -> RekognitionAsyncClientBuilder:  # <2>
        return self.configure(RekognitionAsyncClient.builder().httpClient(http_client))  # <1>

    @Bean(preDestroy="close")
    @Requires(beans=SdkAsyncHttpClient)
    def async_client(self, builder: RekognitionAsyncClientBuilder) -> RekognitionAsyncClient:  # <3>
        return builder.build()

    def configure(self, builder):
        builder.region(self.region_provider.getRegion()).credentialsProvider(self.credentials_provider)
        if self.user_agent_provider is not None:
            builder.overrideConfiguration(ClientOverrideConfiguration.builder()
                                          .putAdvancedOption(SdkAdvancedClientOption.USER_AGENT_SUFFIX, self.user_agent_provider.userAgent())
                                          .build())
        if self.aws_service_configuration is not None and self.aws_service_configuration.getEndpointOverride() is not None:
            builder.endpointOverride(self.aws_service_configuration.getEndpointOverride())
        return builder
# end::class[]
