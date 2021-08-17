package io.micronaut.docs.function.aws

import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.function.aws.MicronautRequestStreamHandler
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Specification

@MicronautTest
class ContextFunctionNameSpec extends Specification {
    void "Context::getFunctionName has priority over micronaut.function.name property"() {
        given:
        ApplicationContext ctx = ApplicationContext.run(['micronaut.function.name': 'capitalize'])
        MicronautRequestStreamHandler handler = new MicronautRequestStreamHandler(ctx)

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
        output.toString() == 'JOHN'

        cleanup:
        handler.close()
    }
}
