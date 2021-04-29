package io.micronaut.aws.xray.strategy

import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

class SystemPropertySegmentNamingStrategyConditionSpec extends Specification {
    @RestoreSystemProperties
    void 'SystemPropertySegmentNamingStrategyCondition evaluates to true if com.amazonaws.xray.strategy.tracingName sys prop is set'() {
        given:
        System.setProperty("com.amazonaws.xray.strategy.tracingName", 'foo')

        expect:
        new SystemPropertySegmentNamingStrategyCondition().matches(null)
    }
}
