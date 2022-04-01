package io.micronaut.function.aws.proxy.test

import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.http.*
import io.micronaut.http.annotation.*
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest
class DeleteWithoutBodySpec extends Specification {
    @Inject
    @Client('/')
    HttpClient client

    void "verifies it is possible to exposes a delete endpoint which is invoked without a body"() {
        given:
        HttpRequest<?> request = HttpRequest.DELETE("/sessions/sergio")

        when:
        HttpResponse<?> response = client.toBlocking().exchange(request)

        then:
        noExceptionThrown()

        and:
        response.status() == HttpStatus.OK
    }

    @Controller('/sessions')
    static class SessionsController {
        @Delete(value = "/{username}", produces = MediaType.TEXT_PLAIN)
        String test() {
            'delete'
        }
    }
}
