package io.micronaut.function.aws.proxy.test

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.http.HttpHeaders
import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.http.cookie.Cookie
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@Property(name = 'spec.name', value = 'CookiesSpec')
@MicronautTest
class CookiesSpec extends Specification {

    @Inject
    @Client("/")
    HttpClient httpClient

    void "each response cookie is sent as its own Set-Cookie header"() {
        when:
        HttpResponse<String> response = httpClient.toBlocking().exchange(HttpRequest.GET('/cookies'), String)

        then:
        response.headers.getAll(HttpHeaders.SET_COOKIE).size() == 2
        response.headers.getAll(HttpHeaders.SET_COOKIE).any { it.startsWith("a=1") && it.contains("Expires=") }
        response.headers.getAll(HttpHeaders.SET_COOKIE).any { it.startsWith("b=2") }
    }

    @Requires(property = 'spec.name', value = 'CookiesSpec')
    @Controller('/cookies')
    static class CookiesController {
        @Get
        HttpResponse<String> index() {
            HttpResponse.ok("ok").cookies([Cookie.of("a", "1").maxAge(3600), Cookie.of("b", "2")] as Set)
        }
    }
}
