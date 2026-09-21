# tag::clazz[]
from abc import ABC, abstractmethod

from micronaut.function.client import FunctionClient


@FunctionClient
class AnalyticsClient(ABC):

    @abstractmethod
    def analytics(self, product_id: str) -> str:
        ...
# end::clazz[]
