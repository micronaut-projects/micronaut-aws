package io.micronaut.docs.function.client.aws

import io.micronaut.context.ApplicationContext
import io.micronaut.function.client.FunctionDefinition
import io.micronaut.function.client.aws.v2.AwsInvokeRequestDefinition
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import jakarta.inject.Inject

@MicronautTest(startApplication = false)
class AnalyticsClientSpec extends Specification {
    @Inject
    ApplicationContext applicationContext

    void "test setup function definitions"() {
        given:
        Collection<FunctionDefinition> definitions = applicationContext.getBeansOfType(FunctionDefinition)

        expect:
        definitions.size() == 1
        definitions.first() instanceof AwsInvokeRequestDefinition

        when:
        AwsInvokeRequestDefinition invokeRequestDefinition = (AwsInvokeRequestDefinition) definitions.first()

        then:
        invokeRequestDefinition.name == 'analytics'
    }
}
