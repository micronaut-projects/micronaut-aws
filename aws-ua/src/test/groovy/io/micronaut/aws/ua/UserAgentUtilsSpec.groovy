package io.micronaut.aws.ua

import io.micronaut.core.version.SemanticVersion
import spock.lang.Specification

class UserAgentUtilsSpec extends Specification {

    void "User-Agent is micronaut/version"() {
        when:
        String result = UserAgentUtils.userAgent()

        then:
        result.startsWith("micronaut/")

        when:
        String micronaut = result.substring("micronaut/".length())
        SemanticVersion semanticVersion = new SemanticVersion(micronaut)

        then:
        noExceptionThrown()
    }
}
