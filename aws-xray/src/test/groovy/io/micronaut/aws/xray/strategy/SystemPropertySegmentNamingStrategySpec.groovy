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
        'foo' == applicationContext.getBean(SegmentNamingStrategy).resolveName(Mock(HttpRequest)).get()
    }
}
