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

import javax.inject.Singleton

class SkillsControllerSecurityExceptionSpec extends EmbeddedServerSpecification implements RequestEnvelopFixture {

    @Override
    String getSpecName() {
        SkillsControllerSecurityExceptionSpec.simpleName
    }

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                "alexa.skills.securityexception.skill-id": "23132234234234324dsf"
        ]
    }

    void "if the verification service throws a Security Exception the endpoint returns 400"() {
        when:
        String skillId = applicationContext.getBean(AlexaSkillConfiguration).skillId
        RequestEnvelope re = launchRequestEnvelop(skillId)

        HttpRequest request = HttpRequest.POST("/alexa", re)
        client.exchange(request, ResponseEnvelope)

        then:
        HttpClientResponseException e = thrown()
        e.status == HttpStatus.BAD_REQUEST
    }

    @Requires(property = 'spec.name', value = 'SkillsControllerSecurityExceptionSpec')
    @Primary
    @Singleton
    static class CustomRequestEnvelopeVerificationService implements RequestEnvelopeVerificationService {

        @Override
        void verify(HttpHeaders httpHeaders, byte[] serializedRequestEnvelope, RequestEnvelope requestEnvelope) throws SecurityException, AskSdkException {
            throw new SecurityException()
        }
    }

    @Requires(property = 'spec.name', value = 'SkillsControllerSecurityExceptionSpec')
    @Singleton
    static class CustomLaunchRequestHandler extends LaunchRequestHandler {}
}
