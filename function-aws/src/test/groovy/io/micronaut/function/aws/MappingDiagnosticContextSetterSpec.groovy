package io.micronaut.function.aws

import com.amazonaws.services.lambda.runtime.Context
import edu.umd.cs.findbugs.annotations.NonNull
import io.micronaut.context.ApplicationContextBuilder
import spock.lang.Specification

class MappingDiagnosticContextSetterSpec extends Specification {
    static class CustomSquareHandler extends SquareHandler {
        Map<String, Object> values = [:]
       
        @Override
        protected void mdcput(@NonNull String key, @NonNull String value) {
            values.put(key, value)
        }
    }

    void "context cannot be null"() {
        given:
        MicronautRequestHandler handler = new CustomSquareHandler()
        String awsRequestId = '6bc28136-xmpl-4365-b021-0ce6b2e64ab0'

        when:
        def stubContext = Stub(Context){
            getAwsRequestId() >> awsRequestId
        }
        Integer input = 12
        handler.handleRequest(input, stubContext)

        then:
        noExceptionThrown()

        handler.values['AWSRequestId'] == awsRequestId
    }

}
