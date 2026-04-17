package io.micronaut.aws.distributedconfiguration.imports

import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import spock.lang.Specification

class AwsConfigImportContextFactorySpec extends Specification {

    void "child context disables bootstrap and config client while propagating app name and active environments"() {
        given:
        ApplicationContext parent = ApplicationContext.run([
            'micronaut.application.name': 'demo-app',
            'micronaut.config-client.enabled': true
        ], 'test')
        AwsConfigImportContextFactory factory = new AwsConfigImportContextFactory()

        when:
        ApplicationContext child = factory.build(parent.environment, [
            'aws.secretsmanager.enabled': true
        ])

        then:
        child.environment.activeNames.contains('test')
        child.environment.getProperty('micronaut.application.name', String).orElse(null) == 'demo-app'
        child.environment.getProperty('micronaut.config-client.enabled', Boolean).orElse(false) == false
        child.environment.getProperty(Environment.BOOTSTRAP_CONTEXT_PROPERTY, Boolean).orElse(false) == false
        child.environment.getProperty('aws.secretsmanager.enabled', Boolean).orElse(false)
        !child.environment.getProperty('aws.client.system-manager.parameterstore.enabled', Boolean).orElse(false)

        cleanup:
        child.close()
        parent.close()
    }

    void "child context can activate additional environments for provider-specific bean requirements"() {
        given:
        ApplicationContext parent = ApplicationContext.run([
            'micronaut.application.name': 'demo-app'
        ], 'test')
        AwsConfigImportContextFactory factory = new AwsConfigImportContextFactory()

        when:
        ApplicationContext child = factory.build(parent.environment, [
            'aws.client.system-manager.parameterstore.enabled': true
        ], Environment.AMAZON_EC2)

        then:
        child.environment.activeNames.contains('test')
        child.environment.activeNames.contains(Environment.AMAZON_EC2)
        child.environment.getProperty('aws.client.system-manager.parameterstore.enabled', Boolean).orElse(false)
        !child.environment.getProperty('aws.secretsmanager.enabled', Boolean).orElse(false)

        cleanup:
        child.close()
        parent.close()
    }
}
