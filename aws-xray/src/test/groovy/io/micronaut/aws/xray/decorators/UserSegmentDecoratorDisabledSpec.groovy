package io.micronaut.aws.xray.decorators

import io.micronaut.aws.xray.ApplicationContextSpecification
import io.micronaut.core.util.StringUtils

class UserSegmentDecoratorDisabledSpec extends ApplicationContextSpecification {

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                'tracing.xray.user-segment-decorator': StringUtils.FALSE,
        ]
    }

    void "Bean of type UserSegmentDecorator does not exists if xray.tracing.user-segment-decorator is false"() {
        expect:
        !applicationContext.containsBean(UserSegmentDecorator)
    }
}
