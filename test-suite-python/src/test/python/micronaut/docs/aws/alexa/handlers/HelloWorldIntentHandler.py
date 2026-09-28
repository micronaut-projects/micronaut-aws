# tag::clazz[]
from com.amazon.ask.dispatcher.request.handler import HandlerInput, RequestHandler
from com.amazon.ask.model import Response
from com.amazon.ask.request import Predicates
from jakarta.inject import Singleton
from java.util import Optional


@Singleton  # <1>
class HelloWorldIntentHandler(RequestHandler):

    def canHandle(self, input: HandlerInput) -> bool:
        return input.matches(Predicates.intentName("HelloWorldIntent"))

    def handle(self, input: HandlerInput) -> Optional[Response]:
        speech_text = "Hello world"
        return (input.getResponseBuilder()
                .withSpeech(speech_text)
                .withSimpleCard("HelloWorld", speech_text)
                .build())
# end::clazz[]
