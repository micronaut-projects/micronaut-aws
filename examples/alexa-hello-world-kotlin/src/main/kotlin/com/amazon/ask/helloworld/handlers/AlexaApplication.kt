package com.amazon.ask.helloworld.handlers

// tag::imports[]
import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.model.Response
import io.micronaut.function.aws.alexa.Intents
import io.micronaut.function.aws.alexa.annotation.IntentHandler

import javax.inject.Singleton
import java.util.Optional
// end::imports[]

// tag::class[]
@Singleton // <1>
class AlexaApplication(val messageService: MessageService) { // <2>

    companion object {
        const val INTENT_NAME = "HelloWorldIntent"
    }

    @IntentHandler(INTENT_NAME) // <3>
    fun greet(input : HandlerInput) : Optional<Response> { // <4>
        val speechText = messageService.sayHello()
        return input.responseBuilder
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .build()
    }
// end::class[]

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
