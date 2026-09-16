package io.micronaut.docs.aws.alexa.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.Intent;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.LaunchRequest;
import com.amazon.ask.model.Request;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.ui.SimpleCard;
import com.amazon.ask.model.ui.SsmlOutputSpeech;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class HelloWorldIntentHandlerTest {

    @Inject
    HelloWorldIntentHandler handler;

    @Test
    void handlesTheHelloWorldIntent() {
        HandlerInput input = handlerInput(IntentRequest.builder()
                .withIntent(Intent.builder().withName("HelloWorldIntent").build())
                .build());

        assertTrue(handler.canHandle(input));
        assertFalse(handler.canHandle(handlerInput(LaunchRequest.builder().build())));

        Optional<Response> response = handler.handle(input);

        assertTrue(response.isPresent());
        SsmlOutputSpeech speech = assertInstanceOf(SsmlOutputSpeech.class, response.get().getOutputSpeech());
        assertEquals("<speak>Hello world</speak>", speech.getSsml());
        SimpleCard card = assertInstanceOf(SimpleCard.class, response.get().getCard());
        assertEquals("HelloWorld", card.getTitle());
        assertEquals("Hello world", card.getContent());
    }

    private static HandlerInput handlerInput(Request request) {
        return HandlerInput.builder()
                .withRequestEnvelope(RequestEnvelope.builder().withRequest(request).build())
                .build();
    }
}
