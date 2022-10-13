package io.micronaut.aws.ua

import io.micronaut.core.version.SemanticVersion
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class UserAgentProviderSpec extends Specification {

    @Inject
    UserAgentProvider userAgentProvider

    void "User-Agent is micronaut/version"() {
        when:
        String result = userAgentProvider.userAgent()

        then:
        result.startsWith("micronaut/")

        when:
        String micronaut = result.substring("micronaut/".length())
        SemanticVersion semanticVersion = new SemanticVersion(micronaut)

        then:
        noExceptionThrown()
    }
}
