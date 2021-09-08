package io.micronaut.function.aws

import io.micronaut.context.env.Environment
import io.micronaut.core.util.StringUtils
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

class DefaultEnvironmentsSpec extends Specification {

    @RestoreSystemProperties
    void "function and lambda are the default environments"() {
        given: "don't deduce environments so that test environment is not detected"
        System.setProperty(Environment.DEDUCE_ENVIRONMENT_PROPERTY, StringUtils.FALSE)
        MockHandler handler = new MockHandler()

        expect:
        ['function', 'lambda'] as Set<String> == handler.applicationContext.environment.activeNames
    }

    @RestoreSystemProperties
    void "if the user provides an environment lambda and function are not registered as environments"() {
        given:
        System.setProperty(Environment.ENVIRONMENTS_PROPERTY, "foo")
        MockHandler handler = new MockHandler()

        expect:
        ['foo', 'test'] as Set<String> == handler.applicationContext.environment.activeNames
    }

    static class MockHandler extends MicronautRequestHandler<Void, Void> {

        @Override
        Void execute(Void input) {
            return null
        }
    }
}
