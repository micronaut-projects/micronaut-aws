package io.micronaut.aws.xray.strategy

import io.micronaut.aws.xray.ApplicationContextSpecification
import io.micronaut.http.HttpRequest
import spock.util.environment.RestoreSystemProperties

class SystemPropertySegmentNamingStrategySpec extends ApplicationContextSpecification {

    @RestoreSystemProperties
    void 'tracing.xray.fixed-name enabled FixedSegmentNamingStrategy'() {
        given:
        System.setProperty("com.amazonaws.xray.strategy.tracingName", 'foo')

        expect:
        applicationContext.containsBean(SegmentNamingStrategy)
        applicationContext.getBeansOfType(SegmentNamingStrategy).first().nameForRequest(Mock(HttpRequest)) == 'foo'
    }
}
