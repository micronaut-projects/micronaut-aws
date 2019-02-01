package com.amazon.ask.helloworld.handlers

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.model.Response
import io.micronaut.function.aws.alexa.Intents
import io.micronaut.function.aws.alexa.annotation.IntentHandler

import javax.inject.Singleton
import java.util.Optional

@Singleton
class AlexaApplication(val messageService: MessageService) {

    companion object {
        const val INTENT_NAME = "HelloWorldIntent"
    }

    @IntentHandler(INTENT_NAME)
    fun greet(input : HandlerInput) : Optional<Response> {
        val speechText = messageService.sayHello()
        return input.responseBuilder
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .build()
    }

    @IntentHandler(Intents.HELP)
    fun help(input : HandlerInput ) : Optional<Response> {
        val speechText = "You can say hello to me!"
        return input.responseBuilder
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .withReprompt(speechText)
                .build()
    }

    @IntentHandler(Intents.FALLBACK)
    fun fallback(input : HandlerInput) : Optional<Response>  {
        val speechText = "Sorry, I don't know that. You can say try saying help!"
        return input.responseBuilder
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .withReprompt(speechText)
                .build()
    }

    @IntentHandler(Intents.CANCEL, Intents.STOP)
    fun cancel(input : HandlerInput) : Optional<Response> {
        return input.responseBuilder
                .withSpeech("Goodbye")
                .withSimpleCard("HelloWorld", "Goodbye")
                .build()
    }
}
