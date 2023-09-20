package io.micronaut.function.aws.proxy

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent
import io.micronaut.core.convert.ConversionService
import io.micronaut.http.CaseInsensitiveMutableHttpHeaders
import io.micronaut.http.HttpHeaders
import io.micronaut.http.MutableHttpHeaders
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class MapCollapseUtilsSpec extends Specification {

    @Inject
    ConversionService conversionService

    /**
     *
     * GET /prod/ HTTP/1.1
     * Key: value1,value2,value3
     * Foo: Bar
     */
    void "payload v2 example"() {
        given:
        APIGatewayV2HTTPEvent event = new APIGatewayV2HTTPEvent()
        Map<String, String> headers = new HashMap<>();
        headers.put(HttpHeaders.DATE, "Wed, 21 Oct 2015 07:28:00 GMT")
        headers.put("foo", "Bar")
        headers.put("key", "value1,value2,value3")
        headers.put(HttpHeaders.USER_AGENT, "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko)")
        event.setHeaders(headers)

        when:
        MutableHttpHeaders httpHeaders = new CaseInsensitiveMutableHttpHeaders(MapCollapseUtils.collapse(Collections.emptyMap(), event.getHeaders()), conversionService);

        then:
        noExceptionThrown()

        and:
        "Bar" == httpHeaders.get("Foo")
        ["value1", "value2", "value3"] == httpHeaders.getAll("Key")
        "Mozilla/5.0 (X11; Linux x86_64) AppleWebKit/537.36 (KHTML, like Gecko)" == httpHeaders.get(HttpHeaders.USER_AGENT)
        "Wed, 21 Oct 2015 07:28:00 GMT" == httpHeaders.get(HttpHeaders.DATE)
    }

    /**
     *
     * GET /prod/ HTTP/1.1
     * Key: value1,value2,value3
     * Foo: Bar
     */
    void "payload v1 example"() {
        given:
        APIGatewayProxyRequestEvent request = new APIGatewayProxyRequestEvent()
        Map<String, String> headers = new HashMap<>()
        headers.put("Foo", "Bar")
        headers.put("Key", "value1,value2,value3")
        request.setHeaders(headers)
        Map<String, List<String>> multiValueHeaders = new HashMap<>()
        multiValueHeaders.put("Key", Arrays.asList("value1", "value2", "value3"))
        multiValueHeaders.put("Foo", Collections.singletonList("Bar"))
        request.setMultiValueHeaders(multiValueHeaders)

        when:
        MutableHttpHeaders httpHeaders = new CaseInsensitiveMutableHttpHeaders(MapCollapseUtils.collapse(request.getMultiValueHeaders(), request.getHeaders()), conversionService);

        then:
        noExceptionThrown()

        and:
        "Bar" == httpHeaders.get("Foo")
        ["value1", "value2", "value3"] == httpHeaders.getAll("Key")

    }
}
