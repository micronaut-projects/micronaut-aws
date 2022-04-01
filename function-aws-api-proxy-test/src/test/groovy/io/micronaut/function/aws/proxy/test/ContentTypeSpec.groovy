package io.micronaut.function.aws.proxy.test

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Status
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.PendingFeature
import spock.lang.Specification

@Property(name = 'spec.name', value = 'ContentTypeSpec')
@MicronautTest
class ContentTypeSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient httpClient

    @PendingFeature
    void "verify controllers return json by default"() {
        given:
        HttpRequest<?> request = HttpRequest.GET('/json/bydefault').accept(MediaType.APPLICATION_JSON)

        when:
        HttpResponse<String> response = httpClient.toBlocking().exchange(request, String)

        then:
        HttpStatus.OK == response.status()
        response.body.get() == '{"msg":"Hello world"}'
        response.headers
        "application/json" == response.headers.get("Content-Type")
    }

    @Controller('/json')
    @Requires(property = 'spec.name', value = 'ContentTypeSpec')
    static class BodyController {

        @Get("/bydefault")
        @Status(HttpStatus.OK)
        Map<String, Object> index() {
            [msg: "Hello world"]
        }
    }
}

