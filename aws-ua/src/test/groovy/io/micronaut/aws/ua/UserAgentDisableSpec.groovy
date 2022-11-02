package io.micronaut.aws.ua

import io.micronaut.context.BeanContext
import io.micronaut.context.annotation.Property
import io.micronaut.core.util.StringUtils
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@Property(name = "aws.ua.enabled", value = StringUtils.FALSE)
@MicronautTest
class UserAgentDisableSpec extends Specification {
    @Inject
    BeanContext beanContext

    void "bean of type UserAgentProvider does not exist"() {
        expect:
        !beanContext.containsBean(UserAgentProvider)
    }
}
