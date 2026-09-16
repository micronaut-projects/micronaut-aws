from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test
from software.amazon.awssdk.services.rekognition import RekognitionAsyncClient, RekognitionClient


@Property(name="spec.name", value="AwsClientFactoryTest")
@MicronautTest(startApplication=False)
class AwsClientFactoryTest:
    client: Annotated[RekognitionClient, Inject]
    async_client: Annotated[RekognitionAsyncClient, Inject]

    @Test
    def test_it_can_create_sync_clients(self):
        assert self.client.serviceName() == "rekognition"

    @Test
    def test_it_can_create_async_clients(self):
        assert self.async_client.serviceName() == "rekognition"
