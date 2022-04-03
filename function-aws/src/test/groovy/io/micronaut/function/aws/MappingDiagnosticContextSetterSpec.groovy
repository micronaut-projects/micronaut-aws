package io.micronaut.function.aws

import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.context.ApplicationContext
import io.micronaut.context.ApplicationContextBuilder
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.NonNull
import jakarta.inject.Singleton
import spock.lang.Specification

class MappingDiagnosticContextSetterSpec extends Specification {

    void "context cannot be null"() {
        given:
        ApplicationContextBuilder builder = ApplicationContext.builder()
                .properties(Collections.singletonMap(
                        "spec.name", "MappingDiagnosticContextSetterSpec"
                ))
        MicronautRequestHandler handler = new SquareHandler(builder)
        String awsRequestId = '6bc28136-xmpl-4365-b021-0ce6b2e64ab0'

        when:
        def stubContext = Stub(Context) {
            getAwsRequestId() >> awsRequestId
        }
        Integer input = 12
        handler.handleRequest(input, stubContext)

        then:
        noExceptionThrown()

        handler.applicationContext.getBean(DiagnosticInfoPopulatorReplacement).values['AWSRequestId'] == awsRequestId
    }

    @Requires(property = "spec.name", value = "MappingDiagnosticContextSetterSpec")
    @Replaces(DiagnosticInfoPopulator)
    @Singleton
    static class DiagnosticInfoPopulatorReplacement extends DefaultDiagnosticInfoPopulator {
        Map<String, Object> values = [:]

        @Override
        protected void mdcput(@NonNull String key, @NonNull String value) {
            values.put(key, value)
        }
    }

    @Requires(property = "spec.name", value = "MappingDiagnosticContextSetterSpec")
    @Singleton
    @Replaces(SquareService)
    static class MockSquareService implements SquareService {
        @Override
        int square(int input) {
            return input * input;
        }
    }

}
