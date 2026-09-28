# tag::clazz[]
from micronaut.context import ApplicationContext
from micronaut.function.aws import MicronautRequestHandler


class SampleRequestHandler(MicronautRequestHandler[str, str]):

    # Used in tests
    def __init__(self, application_context: ApplicationContext):
        super().__init__(application_context)

    def execute(self, input: str) -> str:
        return "Hello " + input
# end::clazz[]
