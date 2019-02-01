package com.amazon.ask.helloworld.handlers

// tag::imports[]
import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.model.Response
import groovy.transform.CompileStatic
import io.micronaut.function.aws.alexa.AlexaIntents
import io.micronaut.function.aws.alexa.annotation.IntentHandler
import javax.inject.Singleton
// end::imports[]

// tag::class[]
@Singleton // <1>
@CompileStatic
class AlexaApplication {

    private final MessageService messageService

    AlexaApplication(MessageService messageService) { // <2>
        this.messageService = messageService
    }

    @IntentHandler("HelloWorldIntent") // <3>
    Optional<Response> greet(HandlerInput input) { // <4>
        String speechText = messageService.sayHello()
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .build()
    }
// end::class[]

    @IntentHandler(AlexaIntents.HELP)
    Optional<Response> help(HandlerInput input) {
        String speechText = "You can say hello to me!"
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .withReprompt(speechText)
                .build()
    }

    @IntentHandler(AlexaIntents.FALLBACK)
    Optional<Response> fallback(HandlerInput input) {
        String speechText = "Sorry, I don't know that. You can say try saying help!"
        return input.getResponseBuilder()
                .withSpeech(speechText)
                .withSimpleCard("HelloWorld", speechText)
                .withReprompt(speechText)
                .build()
    }

    @IntentHandler([AlexaIntents.CANCEL, AlexaIntents.STOP])
    Optional<Response> cancel(HandlerInput input) {
        return input.getResponseBuilder()
                .withSpeech("Goodbye")
                .withSimpleCard("HelloWorld", "Goodbye")
                .build()
    }
}
