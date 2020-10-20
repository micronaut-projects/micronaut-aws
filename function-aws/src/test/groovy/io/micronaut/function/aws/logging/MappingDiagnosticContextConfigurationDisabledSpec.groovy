package io.micronaut.function.aws.logging

import io.micronaut.context.ApplicationContextBuilder
import io.micronaut.function.aws.SquareHandler

class MappingDiagnosticContextConfigurationDisabledSpec extends ApplicationContextSpecification {

    void "if you set micronaut.aws.lambda.mdc.enabled no beans of type MappingDiagnosticContextConfiguration or MappingDiagnosticContextSetter exist"() {
        expect:
        !applicationContext.containsBean(MappingDiagnosticContextConfiguration)
        !applicationContext.containsBean(MappingDiagnosticContextSetter)
    }

    static class CustomSquareHandler extends SquareHandler {
        protected ApplicationContextBuilder newApplicationContextBuilder() {
            super.newApplicationContextBuilder().properties(['micronaut.aws.lambda.mdc.enabled': false])
        }
    }

    @Override
    CustomSquareHandler instantiateHandler() {
        new CustomSquareHandler()
    }
}
