from typing import Annotated

from jakarta.inject import Inject
from micronaut.context import ApplicationContext
from micronaut.function.client import FunctionDefinition
from micronaut.function.client.aws.v2 import AwsInvokeRequestDefinition
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from micronaut.docs.function.client.aws.atnamed.AnalyticsClient import AnalyticsClient as AtNamedAnalyticsClient
from micronaut.docs.function.client.aws.methodnamed.AnalyticsClient import AnalyticsClient as MethodNamedAnalyticsClient


@MicronautTest(startApplication=False)
class AnalyticsClientTest:
    application_context: Annotated[ApplicationContext, Inject]

    @Test
    def test_setup_function_definitions(self):
        definitions = self.application_context.getBeansOfType(FunctionDefinition)

        assert len(definitions) == 1
        definition = definitions.iterator().next()
        assert isinstance(definition, AwsInvokeRequestDefinition)
        assert definition.getName() == "analytics"

    @Test
    def test_function_clients_are_beans(self):
        assert self.application_context.containsBean(AtNamedAnalyticsClient)
        assert self.application_context.containsBean(MethodNamedAnalyticsClient)
