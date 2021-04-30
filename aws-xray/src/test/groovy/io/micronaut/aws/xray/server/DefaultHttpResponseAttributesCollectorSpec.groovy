package io.micronaut.aws.xray.server

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class DefaultHttpResponseAttributesCollectorSpec extends Specification {

    @Shared
    DefaultHttpResponseAttributesCollector httpResponseAttributesCollector = new DefaultHttpResponseAttributesCollector()

    @Unroll
    void 'DefaultHttpResponseAttributesCollector::parseErrorCategory'(HttpStatus httpStatus, ErrorCategory expected) {
        given:
        def response = Stub(HttpResponse) {
            status() >> httpStatus
        }

        when:
        Optional<ErrorCategory> errorCategoryOptional = httpResponseAttributesCollector.parseErrorCategory(response)

        then:
        if (expected) {
            assert errorCategoryOptional.isPresent()
            assert errorCategoryOptional.get() == expected
        } else {
            assert !errorCategoryOptional.isPresent()
        }

        where:
        httpStatus                          || expected
        HttpStatus.OK                       || null
        HttpStatus.FORBIDDEN                || ErrorCategory.FAULT
        HttpStatus.INTERNAL_SERVER_ERROR    || ErrorCategory.ERROR
        HttpStatus.TOO_MANY_REQUESTS        || ErrorCategory.THROTTLE
    }
}
