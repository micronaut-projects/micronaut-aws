from jakarta.inject import Singleton
from micronaut.context.annotation import Requires
from micronaut.context.event import BeanCreatedEvent, BeanCreatedEventListener
from software.amazon.awssdk.core.client.config import ClientOverrideConfiguration
from software.amazon.awssdk.core.retry import RetryMode
from software.amazon.awssdk.services.s3 import S3ClientBuilder


@Requires(property="spec.name", value="S3ClientBuilderTest")
# tag::listener[]
@Singleton
class S3ClientBuilderListener(BeanCreatedEventListener[S3ClientBuilder]):

    def onCreated(self, event: BeanCreatedEvent[S3ClientBuilder]) -> S3ClientBuilder:
        builder = event.getBean()
        builder.overrideConfiguration(ClientOverrideConfiguration.builder().retryPolicy(RetryMode.LEGACY).build())

        return builder
# end::listener[]
