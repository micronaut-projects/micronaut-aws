package io.micronaut.docs.function.aws

import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.function.aws.MicronautRequestStreamHandler
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

@MicronautTest
class MicronautRequestStreamHandlerOverrideResolveFunctionNameSpec extends Specification {
    void "Overriding MicronautRequestStreamHandler::resolveFunctionName(Environment) takes precedence"() {
        given:
        ApplicationContext ctx = ApplicationContext.run(['micronanut.function.name': 'capitalize'])
        MicronautRequestStreamHandler handler = new MicronautRequestStreamHandler(ctx) {
            @Override
            protected String resolveFunctionName(Environment environment) {
                return "lowercase"
            }
        }

        when:
        String input = 'jOHn'
        ByteArrayInputStream inputStream = new ByteArrayInputStream(input.bytes)
        ByteArrayOutputStream output = new ByteArrayOutputStream()
        handler.handleRequest(
                inputStream,
                output,
                Stub(Context) {
            getFunctionName() >> 'uppercase'
        }
        )

        then:
        output.toString() == 'john'

        cleanup:
        handler.close()
    }
}
