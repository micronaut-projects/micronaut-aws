# tag::clazz[]
from typing import Annotated

import java
from jakarta.inject import Inject
from micronaut.context import ApplicationContext
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

# TODO(python): java.type needed because the Java handler compiled from this project's src/test/java is not on the
# Python compile classpath, so no importable module is generated for it; see DISABLED_TESTS.md
SampleRequestHandler = java.type("io.micronaut.docs.function.aws.test.SampleRequestHandler")


@MicronautTest(environments=["function", "lambda"])
class RequestHandlerTest:
    context: Annotated[ApplicationContext, Inject]

    @Test
    def test_handler(self):
        sample_request_handler = SampleRequestHandler(self.context)
        assert sample_request_handler.execute("world") == "Hello world"
# end::clazz[]
