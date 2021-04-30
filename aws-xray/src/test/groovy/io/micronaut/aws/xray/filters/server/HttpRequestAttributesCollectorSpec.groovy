package io.micronaut.aws.xray.filters.server

import io.micronaut.aws.xray.ApplicationContextSpecification
import io.micronaut.aws.xray.MockHttpHeaders
import io.micronaut.aws.xray.filters.HttpRequestAttributesCollector
import io.micronaut.http.HttpRequest
import spock.lang.Shared
import spock.lang.Subject

class HttpRequestAttributesCollectorSpec extends ApplicationContextSpecification {

    @Shared
    @Subject
    HttpRequestAttributesCollector attributesBuilder = applicationContext.getBean(HttpRequestAttributesCollector)

    void 'HttpRequestAttributesBuilder collects a Map of attributes from a HTTP request'() {
        given:
        def request = Stub(HttpRequest) {
            getHeaders() >> new MockHttpHeaders([
                    'Connection': ['upgrade'],
                    'Host': ['3.219.120.115'],
                    'X-Real-IP': ['172.31.45.61'],
                    'X-Forwarded-For': ['23.129.64.240', '172.31.45.61'],
                    'X-Forwarded-Proto': ['https'],
                    'X-Forwarded-Port': ['443'],
                    'X-Amzn-Trace-Id': ['Root=1-608abb4d-6121d00360514b3309494ea4'],
                    'User-Agent': ['Mozilla/5.0 (compatible; Baiduspider/2.0; +http://www.baidu.com/search/spider.html)'],
                    'Accept': ['*/*'],
                    'Accept-Encoding': ['gzip'],
            ])
            getUri() >> new URI("http://localhost:8080")
        }

        when:
        Set<String> attributeKeys = [
                'method',
                'client_ip',
                'url',
                'user_agent',
                'x_forwarded_for'
        ]
        Map<String, Object> result = attributesBuilder.requestAttributes(request)

        then:
        result.size() == attributeKeys.size()
        result.keySet() == attributeKeys
    }
}
