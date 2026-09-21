from typing import Annotated

from jakarta.inject import Named, Singleton
from micronaut.aws.sdk.v2.service import AWSServiceConfiguration, AwsClientFactory
from micronaut.aws.ua import UserAgentProvider
from micronaut.context.annotation import Bean, Factory, Requires
from software.amazon.awssdk.auth.credentials import AwsCredentialsProviderChain
from software.amazon.awssdk.http import SdkHttpClient
from software.amazon.awssdk.http.async_ import SdkAsyncHttpClient
from software.amazon.awssdk.regions.providers import AwsRegionProviderChain
from software.amazon.awssdk.services.rekognition import (
    RekognitionAsyncClient,
    RekognitionAsyncClientBuilder,
    RekognitionClient,
    RekognitionClientBuilder,
)


@Requires(property="spec.name", value="AwsClientFactoryTest")
# tag::class[]
@Factory
class RekognitionClientFactory(AwsClientFactory[RekognitionClientBuilder, RekognitionAsyncClientBuilder, RekognitionClient, RekognitionAsyncClient]):

    def __init__(self,
                 credentials_provider: AwsCredentialsProviderChain,
                 region_provider: AwsRegionProviderChain,
                 user_agent_provider: UserAgentProvider | None,
                 aws_service_configuration: Annotated[AWSServiceConfiguration | None, Named("rekognition")]):
        super().__init__(credentials_provider, region_provider, user_agent_provider, aws_service_configuration)

    # Sync client
    def createSyncBuilder(self) -> RekognitionClientBuilder:  # <1>
        return RekognitionClient.builder()

    @Singleton
    def syncBuilder(self, http_client: SdkHttpClient) -> RekognitionClientBuilder:  # <2>
        return super().syncBuilder(http_client)

    @Bean(preDestroy="close")
    def syncClient(self, builder: RekognitionClientBuilder) -> RekognitionClient:  # <3>
        return super().syncClient(builder)

    # Async client
    def createAsyncBuilder(self) -> RekognitionAsyncClientBuilder:  # <1>
        return RekognitionAsyncClient.builder()

    @Singleton
    @Requires(beans=SdkAsyncHttpClient)
    def asyncBuilder(self, http_client: SdkAsyncHttpClient) -> RekognitionAsyncClientBuilder:  # <2>
        return super().asyncBuilder(http_client)

    @Bean(preDestroy="close")
    @Requires(beans=SdkAsyncHttpClient)
    def asyncClient(self, builder: RekognitionAsyncClientBuilder) -> RekognitionAsyncClient:  # <3>
        return super().asyncClient(builder)
# end::class[]
