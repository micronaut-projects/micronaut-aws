from typing import Annotated

from jakarta.inject import Inject
from micronaut.context.annotation import Property
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test
from software.amazon.awssdk.core.retry import RetryMode
from software.amazon.awssdk.services.s3 import S3ClientBuilder


@Property(name="spec.name", value="S3ClientBuilderTest")
@MicronautTest(startApplication=False)
class S3ClientBuilderTest:
    builder: Annotated[S3ClientBuilder, Inject]

    @Test
    def test_builders_can_be_customised(self):
        retry_policy = self.builder.overrideConfiguration().retryPolicy()
        assert retry_policy.isPresent()
        assert retry_policy.get().retryMode() == RetryMode.LEGACY
