package io.micronaut.docs.aws.alexa.handlers

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.model.Intent
import com.amazon.ask.model.IntentRequest
import com.amazon.ask.model.LaunchRequest
import com.amazon.ask.model.Request
import com.amazon.ask.model.RequestEnvelope
import com.amazon.ask.model.ui.SimpleCard
import com.amazon.ask.model.ui.SsmlOutputSpeech
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

@MicronautTest(startApplication = false)
class HelloWorldIntentHandlerTest {

    @Inject
    lateinit var handler: HelloWorldIntentHandler

    @Test
    fun handlesTheHelloWorldIntent() {
        val input = handlerInput(IntentRequest.builder()
            .withIntent(Intent.builder().withName("HelloWorldIntent").build())
            .build())

        assertTrue(handler.canHandle(input))
        assertFalse(handler.canHandle(handlerInput(LaunchRequest.builder().build())))

        val response = handler.handle(input)

        assertTrue(response.isPresent)
        val speech = assertInstanceOf(SsmlOutputSpeech::class.java, response.get().outputSpeech)
        assertEquals("<speak>Hello world</speak>", speech.ssml)
        val card = assertInstanceOf(SimpleCard::class.java, response.get().card)
        assertEquals("HelloWorld", card.title)
        assertEquals("Hello world", card.content)
    }

    private fun handlerInput(request: Request): HandlerInput {
        return HandlerInput.builder()
            .withRequestEnvelope(RequestEnvelope.builder().withRequest(request).build())
            .build()
    }
}
