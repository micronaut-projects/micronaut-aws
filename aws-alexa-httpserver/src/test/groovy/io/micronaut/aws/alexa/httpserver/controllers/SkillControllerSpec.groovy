package io.micronaut.aws.alexa.httpserver.controllers

import com.amazon.ask.AlexaSkill
import com.amazon.ask.model.SessionEndedRequest
import com.amazon.ask.model.ui.SsmlOutputSpeech

import static com.amazon.ask.request.Predicates.requestType
import static com.amazon.ask.request.Predicates.intentName
import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.dispatcher.request.handler.RequestHandler
import com.amazon.ask.model.Application
import com.amazon.ask.model.Context
import com.amazon.ask.model.LaunchRequest
import com.amazon.ask.model.Request
import com.amazon.ask.model.RequestEnvelope
import com.amazon.ask.model.Response
import com.amazon.ask.model.ResponseEnvelope
import com.amazon.ask.model.Session
import com.amazon.ask.model.User
import com.amazon.ask.model.interfaces.system.SystemState
import com.amazon.ask.request.Predicates
import io.micronaut.aws.alexa.builders.AlexaSkillBuilder
import io.micronaut.aws.alexa.builders.SkillBuilderProvider
import io.micronaut.aws.alexa.httpserver.EmbeddedServerSpecification
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus

import javax.inject.Singleton
import java.time.OffsetDateTime
import java.time.ZoneId

import static com.amazon.ask.util.SdkConstants.FORMAT_VERSION

class SkillControllerSpec extends EmbeddedServerSpecification {

    private static final String LOCALE = "en-US";

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                'alexa.verifiers.signature': false,
                'alexa.verifiers.timestamp': false,
                "alexa.skills.helloworld.skill-id": "23132234234234324dsf",
        ]
    }

    void "doPost passedVerification responseSuccess"() {
        given:
        OffsetDateTime timestamp = OffsetDateTime.ofInstant(new Date().toInstant(), ZoneId.systemDefault())
        LaunchRequest launchRequest = LaunchRequest.builder().withRequestId("rId").withLocale(LOCALE).withTimestamp(timestamp).build()

        expect:
        applicationContext.containsBean(SkillBuilderProvider)
        applicationContext.containsBean(AlexaSkillBuilder)
        applicationContext.containsBean(AlexaSkill)

        when:
        HttpRequest request = HttpRequest.POST("/alexa", buildRequestEnvelope(FORMAT_VERSION, launchRequest, "applicationId"))
        HttpResponse<ResponseEnvelope> response = client.exchange(request, ResponseEnvelope)

        then:
        noExceptionThrown()
        response.status() == HttpStatus.OK

        when:
        ResponseEnvelope responseEnvelope = response.body()

        then:
        responseEnvelope
        responseEnvelope.response.outputSpeech instanceof SsmlOutputSpeech
        ((SsmlOutputSpeech) responseEnvelope.response.outputSpeech).ssml == "<speak>Welcome to the Alexa Skills Kit, you can say hello</speak>"

    }

    RequestEnvelope buildRequestEnvelope(String version, Request request, String applicationId) {
        Application application = Application.builder().withApplicationId(applicationId).build()
        SystemState systemState = SystemState.builder().withApplication(application).build();
        Context context = Context.builder().withSystem(systemState).build()
        RequestEnvelope
                .builder()
                .withContext(context)
                .withVersion(version)
                .withSession(buildSession(application))
                .withRequest(request)
                .build()
    }

    protected Session buildSession(Application application) {
        Session
            .builder()
            .withSessionId("sId")
            .withApplication(application)
            .withUser(User.builder().withUserId("UserId").build())
            .build()
    }

    @Singleton
    static class LaunchRequestHandler implements RequestHandler {

        @Override
        boolean canHandle(HandlerInput handlerInput) {
            return handlerInput.matches(Predicates.requestType(LaunchRequest))
        }

        @Override
        Optional<Response> handle(HandlerInput handlerInput) {
            String speechText = "Welcome to the Alexa Skills Kit, you can say hello";
            return handlerInput.getResponseBuilder()
                    .withSpeech(speechText)
                    .withSimpleCard("HelloWorld", speechText)
                    .withReprompt(speechText)
                    .build();
        }
    }

    @Singleton
    static class HelloWorldIntentHandler implements RequestHandler {

        @Override
        boolean canHandle(HandlerInput input) {
            input.matches(Predicates.intentName("HelloWorldIntent"));
        }

        @Override
        Optional<Response> handle(HandlerInput input) {
            String speechText = "Hello world";
            input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withSimpleCard("HelloWorld", speechText)
                    .build()
        }
    }

    @Singleton
    static class HelpIntentHandler implements RequestHandler {

        @Override
        boolean canHandle(HandlerInput input) {
            input.matches(intentName("AMAZON.HelpIntent"));
        }

        @Override
        Optional<Response> handle(HandlerInput input) {
            String speechText = "You can say hello to me!";
            input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withSimpleCard("HelloWorld", speechText)
                    .withReprompt(speechText)
                    .build()
        }
    }

    @Singleton
    static class CancelandStopIntentHandler implements RequestHandler {

        @Override
        boolean canHandle(HandlerInput input) {
            input.matches(intentName("AMAZON.StopIntent").or(intentName("AMAZON.CancelIntent")));
        }

        @Override
        Optional<Response> handle(HandlerInput input) {
            input.getResponseBuilder()
                    .withSpeech("Goodbye")
                    .withSimpleCard("HelloWorld", "Goodbye")
                    .withShouldEndSession(true)
                    .build();
        }
    }

    @Singleton
    static class FallbackIntentHandler implements RequestHandler {

        @Override
        boolean canHandle(HandlerInput input) {
            return input.matches(intentName("AMAZON.FallbackIntent"))
        }

        @Override
        Optional<Response> handle(HandlerInput input) {
            String speechText = "Sorry, I don't know that. You can say try saying help!";
            return input.getResponseBuilder()
                    .withSpeech(speechText)
                    .withSimpleCard("HelloWorld", speechText)
                    .withReprompt(speechText)
                    .build();
        }
    }

    @Singleton
    static class SessionEndedRequestHandler implements RequestHandler {

        @Override
        boolean canHandle(HandlerInput input) {
            input.matches(requestType(SessionEndedRequest));
        }

        @Override
        Optional<Response> handle(HandlerInput input) {
            // any cleanup logic goes here
            input.getResponseBuilder().build();
        }
    }
}
