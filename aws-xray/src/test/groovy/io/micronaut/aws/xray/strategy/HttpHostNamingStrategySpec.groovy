package io.micronaut.aws.xray.strategy

import io.micronaut.aws.xray.ApplicationContextSpecification
import io.micronaut.aws.xray.MockHttpHeaders
import io.micronaut.http.HttpRequest

class HttpHostNamingStrategySpec extends ApplicationContextSpecification {

    @Override
    Map<String, Object> getConfiguration() {
        super.configuration + [
                'micronaut.server.host-resolution.host-header': 'MyHost'
        ]
    }
    void 'HttpHostNamingStrategy resolves the segment name from the resolved host'() {
        given:
        def request = Stub(HttpRequest) {
            getHeaders() >> new MockHttpHeaders([
                    "MyHost"           : ["abc"],
                    "Forwarded"        : ["host=\"overridden\";proto=overridden"],
                    "X-Forwarded-Host" : ["overridden"],
                    "X-Forwarded-Proto": ["overridden"],
                    "X-Forwarded-Port" : ["overridden"]
            ])
            getUri() >> new URI("http://localhost:8080")
        }

        expect:
        applicationContext.containsBean(SegmentNamingStrategy)
        "http://abc:8080" == applicationContext.getBean(SegmentNamingStrategy).resolveName(request).get()
    }
}
