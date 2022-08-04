package io.micronaut.docs.function.client.aws

import io.micronaut.context.ApplicationContext
import io.micronaut.function.client.FunctionDefinition
import io.micronaut.function.client.aws.AWSInvokeRequestDefinition
import io.micronaut.function.client.aws.AwsInvokeRequestDefinition
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class AnalyticsClientSpec extends Specification {
    @Inject
    ApplicationContext applicationContext

    void "test setup function definitions"() {
        given:
        Collection<FunctionDefinition> definitions = applicationContext.getBeansOfType(FunctionDefinition)
        def awsV1Definition = getInstanceOf(definitions, AWSInvokeRequestDefinition.class)
        def awsV2Definition = getInstanceOf(definitions, AwsInvokeRequestDefinition.class)

        expect:
        definitions.size() == 2
        awsV1Definition.size() == 1
        awsV2Definition.size() == 1

        when:
        AWSInvokeRequestDefinition invokeRequestDefinition = awsV1Definition.first()

        then:
        invokeRequestDefinition.name == 'analytics'
        invokeRequestDefinition.invokeRequest.functionName == 'AwsLambdaFunctionName'
    }

    <T extends FunctionDefinition> Collection<T> getInstanceOf(Collection<FunctionDefinition> defs, Class<T> tClass) {
        return defs.findAll { tClass.isInstance(it) } as Collection<T>
    }
}
