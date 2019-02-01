package com.amazon.ask.helloworld.handlers

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.model.IntentRequest
import com.amazon.ask.model.RequestEnvelope
import io.micronaut.test.annotation.MicronautTest
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import javax.inject.Inject

@MicronautTest
class AlexaApplicationTest {

    @Inject
    lateinit var handler : AlexaApplication

    @Test
    fun testHelloWorldIntent()  {
        val builder = HandlerInput.builder()
        val envelopeBuilder = RequestEnvelope.builder()
        envelopeBuilder.withRequest(IntentRequest.builder().build())
        builder.withRequestEnvelope(envelopeBuilder.build())

        val response = handler.greet(builder.build())
        assertTrue(response.isPresent)

        val outputSpeech = response.get().outputSpeech
        assertTrue(
                outputSpeech.toString().contains(
                        "Hello world"
                )
        )
    }
}
