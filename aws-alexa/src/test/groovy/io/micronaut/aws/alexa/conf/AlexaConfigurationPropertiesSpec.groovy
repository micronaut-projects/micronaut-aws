package io.micronaut.aws.alexa.conf

import io.micronaut.context.ApplicationContext
import io.micronaut.context.exceptions.BeanInstantiationException
import spock.lang.Specification

class AlexaConfigurationPropertiesSpec extends Specification {

    void "skill id cannot be blank"() {
        when:
        ApplicationContext applicationContext = ApplicationContext.run([:], AlexaEnvironment.ENV_ALEXA)

        then:
        !applicationContext.containsBean(AlexaConfiguration)

        when:
        applicationContext.close()
        applicationContext = ApplicationContext.run(['alexa.skill-id': ''], AlexaEnvironment.ENV_ALEXA)
        applicationContext.getBean(AlexaConfiguration)

        then:
        BeanInstantiationException e = thrown()
        e.message.contains('skillId - must not be blank')

        when:
        applicationContext?.close()
        applicationContext = ApplicationContext.run(['alexa.skill-id': 'XXX'], AlexaEnvironment.ENV_ALEXA)

        then:
        applicationContext.containsBean(AlexaConfiguration)

        and:
        applicationContext.getBean(AlexaConfiguration).skillId == 'XXX'

        cleanup:
        applicationContext?.close()
    }
}
