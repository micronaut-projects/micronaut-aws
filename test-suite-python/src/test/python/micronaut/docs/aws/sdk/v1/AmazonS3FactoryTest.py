from typing import Annotated

from com.amazonaws.services.s3 import AmazonS3
from jakarta.inject import Inject
from micronaut.aws.sdk.v1 import EnvironmentAWSCredentialsProvider
from micronaut.context.annotation import Property
from micronaut.context.env import Environment
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test


@Property(name="aws.access-key-id", value="XXXX")
@Property(name="aws.secret-key", value="YYYY")
@MicronautTest(startApplication=False)
class AmazonS3FactoryTest:
    amazon_s3: Annotated[AmazonS3, Inject]
    environment: Annotated[Environment, Inject]

    @Test
    def test_s3_client_uses_the_credentials_of_the_micronaut_environment(self):
        assert self.amazon_s3 is not None
        assert self.amazon_s3.getRegionName() == "us-east-1"

        credentials_provider = EnvironmentAWSCredentialsProvider(self.environment)
        assert credentials_provider.getCredentials().getAWSAccessKeyId() == "XXXX"
        assert credentials_provider.getCredentials().getAWSSecretKey() == "YYYY"
