# tag::clazz[]
from com.amazonaws.services.s3 import AmazonS3, AmazonS3ClientBuilder
from jakarta.inject import Singleton
from micronaut.aws.sdk.v1 import EnvironmentAWSCredentialsProvider
from micronaut.context.annotation import Bean, Factory
from micronaut.context.env import Environment


@Factory
class AmazonS3Factory:

    @Singleton
    @Bean(preDestroy="shutdown")
    def amazon_s3(self, environment: Environment) -> AmazonS3:
        amazon_s3_client_builder = AmazonS3ClientBuilder.standard()
        amazon_s3_client_builder.setCredentials(EnvironmentAWSCredentialsProvider(environment))  # <1>
        return amazon_s3_client_builder.build()
# end::clazz[]
