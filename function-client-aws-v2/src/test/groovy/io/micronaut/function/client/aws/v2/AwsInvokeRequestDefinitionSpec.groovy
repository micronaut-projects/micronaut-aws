package io.micronaut.function.client.aws.v2

import io.micronaut.context.annotation.Property
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@Property(name = "aws.lambda.functions.foo.function-name", value = "x-function-name")
@Property(name = "aws.lambda.functions.foo.qualifier", value = "x-qualifier")
@Property(name = "aws.lambda.functions.foo.client-context", value = "x-client-context")
@Property(name = "aws.lambda.functions.bar.function-name", value = "z-function-name")
@Property(name = "aws.lambda.functions.bar.qualifier", value = "z-qualifier")
@Property(name = "aws.lambda.functions.bar.client-context", value = "z-client-context")
@MicronautTest
class AwsInvokeRequestDefinitionSpec extends Specification {

    @Inject
    List<AwsInvokeRequestDefinition> awsInvokeRequestDefinitions

    void "test aws invoke request"() {
        expect:
        awsInvokeRequestDefinitions.find { it.name == 'foo' }.name == 'foo'
        awsInvokeRequestDefinitions.find { it.name == 'foo' }.functionName == "x-function-name"
        awsInvokeRequestDefinitions.find { it.name == 'foo' }.qualifier == "x-qualifier"
        awsInvokeRequestDefinitions.find { it.name == 'foo' }.clientContext == "x-client-context"

        awsInvokeRequestDefinitions.find { it.name == 'bar' }.name == 'bar'
        awsInvokeRequestDefinitions.find { it.name == 'bar' }.functionName == "z-function-name"
        awsInvokeRequestDefinitions.find { it.name == 'bar' }.qualifier == "z-qualifier"
        awsInvokeRequestDefinitions.find { it.name == 'bar' }.clientContext == "z-client-context"
    }
}
