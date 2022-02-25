package io.micronaut.aws.xray.strategy

import io.micronaut.context.BeanContext
import io.micronaut.core.order.OrderUtil
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

@MicronautTest(startApplication = false)
class SegmentNamingStrategyOrderSpec extends Specification {

    @Inject
    BeanContext beanContext

    @RestoreSystemProperties
    void "verify SegmentNamingStrategy order"() {
        given:
        System.setProperty("com.amazonaws.xray.strategy.tracingName", 'foo')

        when:
        List<SegmentNamingStrategy> strategies = beanContext.getBeansOfType(SegmentNamingStrategy)

        then:
        strategies.get(0) instanceof SystemPropertySegmentNamingStrategy
        strategies.get(1) instanceof FixedSegmentNamingStrategy
        strategies.get(2) instanceof HttpHostNamingStrategy
        strategies.get(3) instanceof CompositeSegmentNamingStrategy

        when: 'env takes precedence over system property strategy'
        List<SegmentNamingStrategy<?>> l = [new SystemPropertySegmentNamingStrategy<>(), new EnvironmentVariableSegmentNamingStrategy<>()]
        OrderUtil.sort(l)

        then:
        l.get(0) instanceof EnvironmentVariableSegmentNamingStrategy
        l.get(1) instanceof SystemPropertySegmentNamingStrategy
    }
}
