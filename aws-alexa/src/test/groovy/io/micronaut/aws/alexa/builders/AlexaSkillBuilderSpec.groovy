package io.micronaut.aws.alexa.builders

import com.amazon.ask.AlexaSkill
import com.amazon.ask.Skill
import com.amazon.ask.Skills
import com.amazon.ask.dispatcher.request.handler.HandlerInput
import com.amazon.ask.dispatcher.request.handler.RequestHandler
import com.amazon.ask.dispatcher.request.interceptor.RequestInterceptor
import com.amazon.ask.dispatcher.request.interceptor.ResponseInterceptor
import com.amazon.ask.model.Response
import com.amazon.ask.request.dispatcher.impl.BaseRequestDispatcher
import io.micronaut.aws.ApplicationContextSpecification
import io.micronaut.aws.alexa.conf.AlexaSkillConfiguration
import io.micronaut.context.annotation.Requires
import io.micronaut.inject.qualifiers.Qualifiers
import spock.lang.Shared
import spock.lang.Subject

import javax.inject.Singleton
import javax.validation.ConstraintViolationException

class AlexaSkillBuilderSpec extends ApplicationContextSpecification {

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + ["alexa.skills.helloworld.skill-id": "23132234234234324dsf"]
    }

    @Subject
    @Shared
    AlexaSkillBuilder alexaSkillBuilder = applicationContext.getBean(AlexaSkillBuilder)

    @Shared
    AlexaSkillConfiguration alexaSkillConfiguration = applicationContext.getBean(AlexaSkillConfiguration, Qualifiers.byName("helloworld"));

    void "Skill builder is a required property"() {
        when:
        alexaSkillBuilder.buildSkill(null, alexaSkillConfiguration)

        then:
        thrown(ConstraintViolationException)
    }

    void "Skill builders registers ResponseInterceptor and Request Interceptors"() {
        when:
        AlexaSkill alexaSkill = alexaSkillBuilder.buildSkill(Skills.standard(), alexaSkillConfiguration)

        then:
        alexaSkill instanceof Skill

        and:
        (((Skill) alexaSkill).requestDispatcher) instanceof BaseRequestDispatcher

        when:
        BaseRequestDispatcher dispatcher  =  (BaseRequestDispatcher)((Skill)alexaSkill).requestDispatcher

        then:
        dispatcher.responseInterceptors.size() == 1
        dispatcher.requestInterceptors.size() == 1
    }

    @Override
    String getSpecName() {
        'AlexaSkillBuilderSpec'
    }

    @Requires(property = 'spec.name', value = 'AlexaSkillBuilderSpec')
    @Singleton
    static class MyResponseInterceptor implements ResponseInterceptor {
    }

    @Requires(property = 'spec.name', value = 'AlexaSkillBuilderSpec')
    @Singleton
    static class MyRequestHandler implements RequestHandler {
        @Override
        boolean canHandle(HandlerInput handlerInput) {
            return true
        }

        @Override
        Optional<Response> handle(HandlerInput handlerInput) {
            return Optional.empty()
        }
    }

    @Requires(property = 'spec.name', value = 'AlexaSkillBuilderSpec')
    @Singleton
    static class MyRequestInterceptor implements RequestInterceptor {
    }
}
