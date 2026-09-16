from typing import Annotated

from jakarta.inject import Inject
from micronaut.function import LocalFunctionRegistry
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .EventLogger import EventLogger


@MicronautTest(startApplication=False)
class EventLoggerTest:
    function_registry: Annotated[LocalFunctionRegistry, Inject]
    event_logger: Annotated[EventLogger, Inject]

    @Test
    def test_the_function_bean_is_registered_as_a_consumer(self):
        consumer = self.function_registry.findConsumer("eventlogger")

        assert consumer.isPresent()
        method = consumer.get()
        assert method.getDeclaringType().getName() == "micronaut.docs.function.aws.EventLogger"
        assert method.getMethodName() == "accept"

        method.invoke(self.event_logger, "hello")
