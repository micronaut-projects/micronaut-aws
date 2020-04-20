package io.micronaut.aws.alexa.httpserver.conf

import io.micronaut.aws.alexa.httpserver.ApplicationContextSpecification
import io.micronaut.aws.alexa.httpserver.controllers.SkillController

class AlexaControllerConfigurationSpec extends ApplicationContextSpecification {

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + ['alexa.endpoint.enabled': false]
    }

    void "Skill controller can be disabled"() {
        expect:
        !applicationContext.containsBean(SkillController)
    }
}
