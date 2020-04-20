package io.micronaut.aws.alexa.ssml

import spock.lang.Specification
import spock.lang.Unroll

class AmazonDomainSpec extends Specification {

    @Unroll("#domain => #expected")
    void "Amazon domain is lowercase"() {
        expect:
        domain.toString() == expected

        where:
        domain             || expected
        AmazonDomain.MUSIC || 'music'
        AmazonDomain.NEWS  || 'news'
    }
}
