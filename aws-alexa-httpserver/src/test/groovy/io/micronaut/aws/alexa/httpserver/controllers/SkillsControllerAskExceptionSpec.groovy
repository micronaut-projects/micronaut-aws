package io.micronaut.aws.alexa.httpserver.controllers

import com.amazon.ask.exception.AskSdkException
import com.amazon.ask.model.RequestEnvelope
import com.amazon.ask.model.ResponseEnvelope
import io.micronaut.aws.alexa.conf.AlexaSkillConfiguration
import io.micronaut.aws.alexa.httpserver.EmbeddedServerSpecification
import io.micronaut.aws.alexa.httpserver.RequestEnvelopFixture
import io.micronaut.aws.alexa.httpserver.controllers.handlers.LaunchRequestHandler
import io.micronaut.aws.alexa.httpserver.services.RequestEnvelopeVerificationService
import io.micronaut.context.annotation.Primary
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.client.exceptions.HttpClientResponseException

import jakarta.inject.Singleton

class SkillsControllerAskExceptionSpec extends EmbeddedServerSpecification implements RequestEnvelopFixture {

    @Override
    String getSpecName() {
        SkillsControllerAskExceptionSpec.simpleName
    }

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                "alexa.skills.securityexception.skill-id": "23132234234234324dsf"
        ]
    }

    void "if the verification service throws a AskException the endpoint returns 500"() {
        when:
        String skillId = applicationContext.getBean(AlexaSkillConfiguration).skillId
        RequestEnvelope re = launchRequestEnvelop(skillId)

        HttpRequest request = HttpRequest.POST("/alexa", re)
        client.exchange(request, ResponseEnvelope)

        then:
        HttpClientResponseException e = thrown()
        e.status == HttpStatus.INTERNAL_SERVER_ERROR
    }

    @Requires(property = 'spec.name', value = 'SkillsControllerAskExceptionSpec')
    @Primary
    @Singleton
    static class CustomRequestEnvelopeVerificationService implements RequestEnvelopeVerificationService {

        @Override
        void verify(HttpHeaders httpHeaders, byte[] serializedRequestEnvelope, RequestEnvelope requestEnvelope) throws SecurityException, AskSdkException {
            throw new AskSdkException()
        }
    }

    @Requires(property = 'spec.name', value = 'SkillsControllerAskExceptionSpec')
    @Singleton
    static class CustomLaunchRequestHandler extends LaunchRequestHandler {}
}
