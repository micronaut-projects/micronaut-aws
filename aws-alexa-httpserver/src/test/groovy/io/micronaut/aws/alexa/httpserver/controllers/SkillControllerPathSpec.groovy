package io.micronaut.aws.alexa.httpserver.controllers

import com.amazon.ask.model.RequestEnvelope
import com.amazon.ask.model.ResponseEnvelope
import com.amazon.ask.model.ui.SsmlOutputSpeech
import io.micronaut.aws.alexa.conf.AlexaSkillConfiguration
import io.micronaut.aws.alexa.httpserver.EmbeddedServerSpecification
import io.micronaut.aws.alexa.httpserver.RequestEnvelopFixture
import io.micronaut.aws.alexa.httpserver.controllers.handlers.CancelStopIntentHandler
import io.micronaut.aws.alexa.httpserver.controllers.handlers.FallbackIntentHandler
import io.micronaut.aws.alexa.httpserver.controllers.handlers.HelloWorldIntentHandler
import io.micronaut.aws.alexa.httpserver.controllers.handlers.HelpIntentHandler
import io.micronaut.aws.alexa.httpserver.controllers.handlers.LaunchRequestHandler
import io.micronaut.aws.alexa.httpserver.controllers.handlers.SessionEndedRequestHandler
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus

import javax.inject.Singleton

class SkillControllerPathSpec extends EmbeddedServerSpecification implements RequestEnvelopFixture {

    @Override
    String getSpecName() {
        SkillControllerPathSpec.simpleName
    }

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                'alexa.endpoint.path': '/computer',
                'alexa.verifiers.signature': false,
                'alexa.verifiers.timestamp': false,
                "alexa.skills.helloworld.skill-id": "23132234234234324dsf"
        ]
    }

    void "doPost passedVerification responseSuccess"() {
        when:
        String skillId = applicationContext.getBean(AlexaSkillConfiguration).skillId
        RequestEnvelope re = launchRequestEnvelop(skillId)

        HttpRequest request = HttpRequest.POST("/computer", re)
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

    @Requires(property = 'spec.name', value = 'SkillControllerPathSpec')
    @Singleton
    static class CustomLaunchRequestHandler extends LaunchRequestHandler {}

    @Requires(property = 'spec.name', value = 'SkillControllerPathSpec')
    @Singleton
    static class CustomHelloWorldIntentHandler extends HelloWorldIntentHandler {}

    @Requires(property = 'spec.name', value = 'SkillControllerPathSpec')
    @Singleton
    static class CustomHelpIntentHandler extends HelpIntentHandler {}

    @Requires(property = 'spec.name', value = 'SkillControllerPathSpec')
    @Singleton
    static class CustomCancelStopIntentHandler extends CancelStopIntentHandler {}

    @Requires(property = 'spec.name', value = 'SkillControllerPathSpec')
    @Singleton
    static class CustomFallbackIntentHandler extends FallbackIntentHandler {}

    @Requires(property = 'spec.name', value = 'SkillControllerPathSpec')
    @Singleton
    static class CustomSessionEndedRequestHandler extends SessionEndedRequestHandler {}
}
