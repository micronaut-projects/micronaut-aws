package io.micronaut.aws.xray.filters.server

import io.micronaut.aws.xray.filters.DefaultHttpResponseAttributesCollector
import io.micronaut.aws.xray.filters.ErrorCategory
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

    @Unroll
    void "#errorCode is #errorCategory"(int errorCode, ErrorCategory errorCategory) {
        expect:
        errorCategory == httpResponseAttributesCollector.parseErrorCategory(errorCode).get()

        where:
        errorCode || errorCategory
        400 || ErrorCategory.FAULT
        401 || ErrorCategory.FAULT
        402 || ErrorCategory.FAULT
        403 || ErrorCategory.FAULT
        403 || ErrorCategory.FAULT
        404 || ErrorCategory.FAULT
        405 || ErrorCategory.FAULT
        406 || ErrorCategory.FAULT
        407 || ErrorCategory.FAULT
        408 || ErrorCategory.FAULT
        409 || ErrorCategory.FAULT
        410 || ErrorCategory.FAULT
        411 || ErrorCategory.FAULT
        412 || ErrorCategory.FAULT
        413 || ErrorCategory.FAULT
        414 || ErrorCategory.FAULT
        415 || ErrorCategory.FAULT
        416 || ErrorCategory.FAULT
        417 || ErrorCategory.FAULT
        418 || ErrorCategory.FAULT
        420 || ErrorCategory.FAULT
        422 || ErrorCategory.FAULT
        423 || ErrorCategory.FAULT
        424 || ErrorCategory.FAULT
        425 || ErrorCategory.FAULT
        426 || ErrorCategory.FAULT
        428 || ErrorCategory.FAULT
        429 || ErrorCategory.THROTTLE
        431 || ErrorCategory.FAULT
        444 || ErrorCategory.FAULT
        450 || ErrorCategory.FAULT
        451 || ErrorCategory.FAULT
        494 || ErrorCategory.FAULT
        500 || ErrorCategory.ERROR
        501 || ErrorCategory.ERROR
        502 || ErrorCategory.ERROR
        503 || ErrorCategory.ERROR
        504 || ErrorCategory.ERROR
        505 || ErrorCategory.ERROR
        506 || ErrorCategory.ERROR
        507 || ErrorCategory.ERROR
        508 || ErrorCategory.ERROR
        509 || ErrorCategory.ERROR
        510 || ErrorCategory.ERROR
        511 || ErrorCategory.ERROR
        522 || ErrorCategory.ERROR
    }
}
