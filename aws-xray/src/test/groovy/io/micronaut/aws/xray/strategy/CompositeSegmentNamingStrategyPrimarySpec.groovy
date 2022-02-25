package io.micronaut.aws.xray.strategy

import io.micronaut.http.HttpRequest
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class CompositeSegmentNamingStrategyPrimarySpec extends Specification {

    @Inject
    SegmentNamingStrategy<HttpRequest<?>> segmentNamingStrategy
    void "CompositeSegmentNamingStrategy is annotated with @Primary"() {
        expect:
        segmentNamingStrategy instanceof CompositeSegmentNamingStrategy
    }
}
