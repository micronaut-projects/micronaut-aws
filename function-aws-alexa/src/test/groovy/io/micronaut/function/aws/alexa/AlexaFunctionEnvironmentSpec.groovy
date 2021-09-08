package io.micronaut.function.aws.alexa

import io.micronaut.context.env.Environment
import io.micronaut.core.util.StringUtils
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

class AlexaFunctionEnvironmentSpec extends Specification {
    @RestoreSystemProperties
    void "function and lambda are the default environments"() {
        given: "don't deduce environments so that test environment is not detected"
        System.setProperty(Environment.DEDUCE_ENVIRONMENT_PROPERTY, StringUtils.FALSE)
        AlexaFunction function = new AlexaFunction()

        expect:
        ['function', 'lambda', 'alexa'] as Set<String> == function.applicationContext.environment.activeNames
    }

    @RestoreSystemProperties
    void "if the user provides an environment, alexa function does not used default environments and uses what the user provided"() {
        given:
        System.setProperty(Environment.ENVIRONMENTS_PROPERTY, "foo")
        AlexaFunction function = new AlexaFunction()

        expect:
        ['foo', 'test'] as Set<String> == function.applicationContext.environment.activeNames
    }
}
