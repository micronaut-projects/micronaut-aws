package com.amazon.ask.helloworld.handlers

import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.model.Response
import io.micronaut.function.aws.alexa.Intents
import io.micronaut.function.aws.alexa.annotation.IntentHandler

import javax.inject.Singleton
import java.util.Optional

@Singleton
class AlexaApplication {

    private final MessageService messageService

    AlexaApplication(MessageService messageService) {
        this.messageService = messageService
    }

    @IntentHandler("HelloWorldIntent")
    Optional<Response> greet(HandlerInput input) {
        String speechText = messageService.sayHello()
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .build()
    }

    @IntentHandler(Intents.HELP)
    Optional<Response> help(HandlerInput input) {
        String speechText = "You can say hello to me!"
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .withReprompt(speechText)
                .build()
    }

    @IntentHandler(Intents.FALLBACK)
    Optional<Response> fallback(HandlerInput input) {
        String speechText = "Sorry, I don't know that. You can say try saying help!"
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .withReprompt(speechText)
                .build()
    }

    @IntentHandler([Intents.CANCEL, Intents.STOP])
    Optional<Response> cancel(HandlerInput input) {
        return input.getResponseBuilder()
                .withSpeech("Goodbye")
                .withSimpleCard("HelloWorld", "Goodbye")
                .build()
    }
}
