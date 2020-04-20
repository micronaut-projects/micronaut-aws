package io.micronaut.aws.alexa.conf

import io.micronaut.context.ApplicationContext
import io.micronaut.context.exceptions.BeanInstantiationException
import spock.lang.Specification

class AlexaSkillConfigurationPropertiesSpec extends Specification {

    void "skill id cannot be blank"() {
        when:
        ApplicationContext applicationContext = ApplicationContext.run([:], AlexaEnvironment.ENV_ALEXA)

        then:
        !applicationContext.containsBean(AlexaSkillConfiguration)

        when:
        applicationContext.close()
        applicationContext = ApplicationContext.run(['alexa.skills.helloworld.skill-id': ''], AlexaEnvironment.ENV_ALEXA)
        applicationContext.getBean(AlexaSkillConfiguration)

        then:
        BeanInstantiationException e = thrown()
        e.message.contains('skillId - must not be blank')

        when:
        applicationContext?.close()
        applicationContext = ApplicationContext.run(['alexa.skills.helloworld.skill-id': 'XXX'], AlexaEnvironment.ENV_ALEXA)

        then:
        applicationContext.containsBean(AlexaSkillConfiguration)

        and:
        applicationContext.getBean(AlexaSkillConfiguration).skillId == 'XXX'

        cleanup:
        applicationContext?.close()
    }
}
