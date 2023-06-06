package io.micronaut.aws.alexa.locale

import io.micronaut.context.BeanContext
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
public class HandlerInputFixedLocaleResolverDisabledSpec extends Specification {

    @Inject
    BeanContext beanContext

    void "HandlerInputFixedLocaleResolver is disabled by default"() {
        expect:
        !beanContext.containsBean(HandlerInputFixedLocaleResolver)
    }

}
