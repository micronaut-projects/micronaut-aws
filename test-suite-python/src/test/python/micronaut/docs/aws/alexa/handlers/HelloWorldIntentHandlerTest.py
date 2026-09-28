from typing import Annotated

import java
from com.amazon.ask.dispatcher.request.handler import HandlerInput
from com.amazon.ask.model import Intent, IntentRequest, LaunchRequest, RequestEnvelope
from com.amazon.ask.model.ui import SimpleCard, SsmlOutputSpeech
from jakarta.inject import Inject
from micronaut.test.extensions.junit5.annotation import MicronautTest
from org.junit.jupiter.api import Test

from .HelloWorldIntentHandler import HelloWorldIntentHandler


@MicronautTest(startApplication=False)
class HelloWorldIntentHandlerTest:
    handler: Annotated[HelloWorldIntentHandler, Inject]

    @Test
    def test_handles_the_hello_world_intent(self):
        input = self.handler_input(IntentRequest.builder()
                                   .withIntent(Intent.builder().withName("HelloWorldIntent").build())
                                   .build())

        assert self.handler.canHandle(input)
        assert not self.handler.canHandle(self.handler_input(LaunchRequest.builder().build()))

        response = self.handler.handle(input)

        assert response.isPresent()
        speech = response.get().getOutputSpeech()
        assert java.instanceof(speech, SsmlOutputSpeech)
        assert speech.getSsml() == "<speak>Hello world</speak>"
        card = response.get().getCard()
        assert java.instanceof(card, SimpleCard)
        assert card.getTitle() == "HelloWorld"
        assert card.getContent() == "Hello world"

    def handler_input(self, request):
        return (HandlerInput.builder()
                .withRequestEnvelope(RequestEnvelope.builder().withRequest(request).build())
                .build())
