package io.micronaut.docs.function.aws

import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.function.aws.MicronautRequestStreamHandler
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

@MicronautTest
class FactoryFunctionBeanSpec extends Specification {
    void "You can create a FunctionBean via a Factory"() {
        given:
        MicronautRequestStreamHandler handler = new MicronautRequestStreamHandler() {
            @Override
            protected String resolveFunctionName(Environment environment) {
                return "reverse"
            }
        }

        when:
        String input = 'jOHn'
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.bytes)
        ByteArrayOutputStream output = new ByteArrayOutputStream()
        handler.handleRequest(
                inputStream,
                output,
                Mock(Context)
        )

        then:
        output.toString() == 'nHOj'

        cleanup:
        handler.close()
    }
}
