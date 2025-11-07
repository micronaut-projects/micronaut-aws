package io.micronaut.function.aws.proxy.test


import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Status
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@Property(name = 'spec.name', value = 'ContentEncodingSpec')
@MicronautTest
class ContentEncodingSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient httpClient

    void "verify that content encoding header is respected"() {
        given:

        def request = HttpRequest.GET('content-encoding/gzip')
                .header(HttpHeaders.ACCEPT_ENCODING, "gzip")
                .accept(MediaType.APPLICATION_JSON)

        when:
        def response = httpClient.toBlocking().exchange(request, String)

        then:
        HttpStatus.OK == response.status()
        response.body.get() == '{"msg":"Hello world"}'
        response.headers
        ["application/json"] == response.headers.getAll("Content-Type")
    }

    @Controller('/content-encoding')
    @Requires(property = 'spec.name', value = 'ContentEncodingSpec')
    static class BodyController {

        @Get("/gzip")
        @Status(HttpStatus.OK)
        Map<String, Object> index() {
            [msg: "Hello world"]
        }
    }
}
