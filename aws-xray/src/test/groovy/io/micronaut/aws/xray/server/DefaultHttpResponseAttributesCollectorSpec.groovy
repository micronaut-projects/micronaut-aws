package io.micronaut.aws.xray.server

import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

class DefaultHttpResponseAttributesCollectorSpec extends Specification {

    @Shared
    DefaultHttpResponseAttributesCollector httpResponseAttributesCollector = new DefaultHttpResponseAttributesCollector()

    void 'DefaultHttpResponseAttributesCollector::responseAttributes extracts attributes from response'() {
        given:
        def response = Stub(HttpResponse) {
            status() >> HttpStatus.TOO_MANY_REQUESTS
            getContentLength() >> -1
        }
        when:
        Map<String, Object> attributes = httpResponseAttributesCollector.responseAttributes(response)

        then:
        attributes == [status: 429]

        when:
        response = Stub(HttpResponse) {
            status() >> HttpStatus.TOO_MANY_REQUESTS
            getContentLength() >> 10000
        }
        attributes = httpResponseAttributesCollector.responseAttributes(response)

        then:
        attributes == [status: 429, content_length: 10000]

    }
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
