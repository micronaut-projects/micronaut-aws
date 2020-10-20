package io.micronaut.function.aws.logging

import com.amazonaws.services.lambda.runtime.Context
import edu.umd.cs.findbugs.annotations.NonNull
import io.micronaut.context.ApplicationContextBuilder
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.function.aws.SquareHandler

import javax.inject.Singleton

class MappingDiagnosticContextSetterSpec extends ApplicationContextSpecification {
    static class CustomSquareHandler extends SquareHandler {
        protected ApplicationContextBuilder newApplicationContextBuilder() {
            super.newApplicationContextBuilder().properties(['spec.name': 'MappingDiagnosticContextSetterSpec'])
        }
    }

    @Override
    CustomSquareHandler instantiateHandler() {
        new CustomSquareHandler()
    }

    void "context cannot be null"() {
        given:
        String awsRequestId = '6bc28136-xmpl-4365-b021-0ce6b2e64ab0'

        when:
        MappingDiagnosticContextSetter setter = applicationContext.getBean(MappingDiagnosticContextSetter)

        then:
        noExceptionThrown()

        when:
        def stubContext = Stub(Context){
            getAwsRequestId() >> awsRequestId
        }
        setter.populateMappingDiagnosticContextValues(stubContext)

        then:
        noExceptionThrown()
        setter instanceof CustomMappingDiagnosticContextSetter

        ((CustomMappingDiagnosticContextSetter)setter).values['AWSRequestId'] == awsRequestId
    }

    @Requires(property = 'spec.name', value = 'MappingDiagnosticContextSetterSpec')
    @Replaces(DefaultMappingDiagnosticContextSetter)
    @Singleton
    static class CustomMappingDiagnosticContextSetter extends DefaultMappingDiagnosticContextSetter {
        Map values = [:]
        CustomMappingDiagnosticContextSetter(MappingDiagnosticContextConfiguration mappingDiagnosticContextConfiguration) {
            super(mappingDiagnosticContextConfiguration)
        }

        @Override
        protected void put(@NonNull String key, @NonNull String val) throws IllegalArgumentException {
            values.put(key, val)
        }
    }
}
