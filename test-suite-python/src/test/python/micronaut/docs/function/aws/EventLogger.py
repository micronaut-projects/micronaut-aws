# tag::clazz[]
import logging

from java.util.function import Consumer
from micronaut.function import FunctionBean

LOG = logging.getLogger(__name__)


@FunctionBean("eventlogger")
class EventLogger(Consumer[str]):

    def accept(self, input: str) -> None:
        LOG.info("Received: %s", input)
# end::clazz[]
