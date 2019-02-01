package com.amazon.ask.helloworld.handlers;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import com.amazon.ask.model.IntentRequest;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.Response;
import com.amazon.ask.model.ui.OutputSpeech;
import io.micronaut.test.annotation.MicronautTest;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.Test;
import javax.inject.Inject;
import java.util.Optional;

@MicronautTest
public class AlexaApplicationTest {

    @Inject
    AlexaApplication handler;

    @Test
    void testHelloWorldIntent() throws Exception {
        final HandlerInput.Builder builder = HandlerInput.builder();
        final RequestEnvelope.Builder envelopeBuilder = RequestEnvelope.builder();
        envelopeBuilder.withRequest(IntentRequest.builder().build());
        builder.withRequestEnvelope(envelopeBuilder.build());
        final Optional<Response> response = handler.greet(builder.build());
        assertTrue(response.isPresent());
        final OutputSpeech outputSpeech = response.get().getOutputSpeech();
        assertTrue(
                outputSpeech.toString().contains(
                        "Hello world"
                )
        );
    }
}
