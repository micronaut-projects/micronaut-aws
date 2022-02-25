package io.micronaut.aws.xray.strategy

import io.micronaut.aws.xray.ApplicationContextSpecification
import io.micronaut.http.HttpRequest

class FixedSegmentNamingStrategySpec extends ApplicationContextSpecification {

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                'tracing.xray.fixed-name': 'micronautapp'
        ]
    }

    void 'tracing.xray.fixed-name enabled FixedSegmentNamingStrategy'() {
        expect:
        applicationContext.containsBean(SegmentNamingStrategy)
        'micronautapp' == applicationContext.getBean(SegmentNamingStrategy).resolveName(Mock(HttpRequest)).get()
    }
}
