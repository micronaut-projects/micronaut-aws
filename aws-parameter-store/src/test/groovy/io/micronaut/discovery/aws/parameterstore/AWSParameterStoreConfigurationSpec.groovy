package io.micronaut.discovery.aws.parameterstore

import io.micronaut.context.ApplicationContext
import spock.lang.Specification

class AWSParameterStoreConfigurationSpec extends Specification {

    void "AWSParameterStoreConfiguration::enabled defaults to true"() {
        given:
        ApplicationContext context = ApplicationContext.run()

        expect:
        context.containsBean(AWSParameterStoreConfiguration)
        context.getBean(AWSParameterStoreConfiguration).enabled

        cleanup:
        context.close()
    }

    void "AWSParameterStoreConfiguration::useSecureParameters defaults to true"() {
        given:
        ApplicationContext context = ApplicationContext.run()

        expect:
        context.containsBean(AWSParameterStoreConfiguration)
        !context.getBean(AWSParameterStoreConfiguration).useSecureParameters

        cleanup:
        context.close()
    }
}
