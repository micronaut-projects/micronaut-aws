# tag::clazz[]
from abc import ABC, abstractmethod

from jakarta.inject import Named
from micronaut.function.client import FunctionClient


@FunctionClient
class AnalyticsClient(ABC):

    @Named("analytics")  # <1>
    @abstractmethod
    def visit(self, product_id: str) -> str:
        ...
# end::clazz[]
