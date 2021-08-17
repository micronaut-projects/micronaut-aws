package io.micronaut.docs.function.aws

import io.micronaut.context.ApplicationContext
import io.micronaut.function.client.FunctionDefinition
import io.micronaut.function.client.aws.AWSInvokeRequestDefinition
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification
import javax.inject.Inject

@MicronautTest(startApplication = false)
class AnalyticsClientSpec extends Specification {
    @Inject
    ApplicationContext applicationContext

    void "test setup function definitions"() {
        given:
        Collection<FunctionDefinition> definitions = applicationContext.getBeansOfType(FunctionDefinition)

        expect:
        definitions.size() == 1
        definitions.first() instanceof AWSInvokeRequestDefinition

        when:
        AWSInvokeRequestDefinition invokeRequestDefinition = (AWSInvokeRequestDefinition) definitions.first()

        then:
        invokeRequestDefinition.name == 'analytics'
        invokeRequestDefinition.invokeRequest.functionName == 'AwsLambdaFunctionName'
    }
}
