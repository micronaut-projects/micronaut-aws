package io.micronaut.aws.secretsmanager

import io.micronaut.context.ApplicationContext
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class ApplicationContextSpecification extends Specification implements ConfigurationFixture {

    @AutoCleanup
    @Shared
    ApplicationContext applicationContext = ApplicationContext.run(configuration)
}
