package io.micronaut.function.aws.alexa

import io.micronaut.context.ApplicationContext

class AlexaConfigurationSpec {

    void 'test that the health indicator configuration is not available when disabled via config'() {
        given:
        ApplicationContext context = ApplicationContext.run(['endpoints.health.discovery-client.enabled': false])

        expect:
        !context.containsBean(AlexaConfiguration)
        !context.containsBean(AlexaConfiguration)

        cleanup:
        context.close()
    }

    void 'test that the health indicator configuration is available when no entry is in config'() {
        given:
        ApplicationContext context = ApplicationContext.run()

        expect:
        context.containsBean(AlexaConfiguration)
        context.containsBean(AlexaConfiguration)

        cleanup:
        context.close()
    }

}
