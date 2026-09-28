# tag::clazz[]
from typing import Annotated

from jakarta.inject import Inject
from micronaut.context import ApplicationContext
from micronaut.function.aws.test.annotation import MicronautLambdaTest
from org.junit.jupiter.api import Test

from .SampleRequestHandler import SampleRequestHandler


@MicronautLambdaTest
class RequestHandlerTest:
    context: Annotated[ApplicationContext, Inject]

    @Test
    def test_handler(self):
        sample_request_handler = SampleRequestHandler(self.context)
        assert sample_request_handler.execute("world") == "Hello world"
# end::clazz[]
