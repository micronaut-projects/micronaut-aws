package io.micronaut.function.aws.proxy.test

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.HttpStatus
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.annotation.QueryValue
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Issue
import spock.lang.Specification

import jakarta.inject.Inject

@MicronautTest
class AwsApiProxyTestServerSpec extends Specification {
    @Inject
    @Client('/')
    HttpClient client

    void 'test invoke function via server'() {
        when:
        String result = client.toBlocking().retrieve('/test')

        then:
        result == 'good'
    }

    void 'test invoke post via server'() {
        when:
        String result = client.toBlocking().retrieve(HttpRequest.POST('/test', "body")
                                        .contentType(MediaType.TEXT_PLAIN), String)

        then:
        result == 'goodbody'
    }

    void 'query values are picked up'() {
        when:
        String result = client.toBlocking().retrieve(HttpRequest.GET('/test-param?foo=bar')
                                        .contentType(MediaType.TEXT_PLAIN), String)

        then:
        result == 'get:bar'
    }

    void 'query values with special chars are not double decoded'() {
        when:
        String result = client.toBlocking().retrieve(HttpRequest.GET('/test-param?foo=prebar%2Bpostbar')
                                        .contentType(MediaType.TEXT_PLAIN), String)

        then:
        result == 'get:prebar+postbar'
    }

    void 'test invoke post that returns empty body'() {
        when:
        HttpResponse<?> response = client.toBlocking()
                .exchange(HttpRequest.POST('/empty-body', "Foo").contentType(MediaType.TEXT_PLAIN))

        then:
        response.status == HttpStatus.NO_CONTENT
        !response.body.isPresent()
    }

    @Issue('https://github.com/micronaut-projects/micronaut-aws/issues/1545')
    void 'can return a ByteArray'() {
        when:
        HttpResponse<?> response = client.toBlocking()
                .exchange(HttpRequest.GET('/byte-array'), byte[])

        then:
        response.status == HttpStatus.OK
        response.body.get() == (1..256).collect { it as byte } as byte[]
    }

    @Controller
    static class TestController {
        @Get(value = '/test', produces = MediaType.TEXT_PLAIN)
        String test() {
            'good'
        }

        @Post(value = '/test', processes = MediaType.TEXT_PLAIN)
        String test(@Body String body) {
            'good' + body
        }

        @Get(value = '/test-param{?foo}', processes = MediaType.TEXT_PLAIN)
        String search(@QueryValue String foo) {
            'get:' + foo
        }

        @Post(value = '/empty-body', processes = MediaType.TEXT_PLAIN)
        HttpResponse emptyBody(@Body String body) {
            return HttpResponse.noContent()
        }

        @Get(value = "/byte-array", produces = MediaType.APPLICATION_OCTET_STREAM)
        HttpResponse<byte[]> byteArray() {
            return HttpResponse.ok((1..256).collect { it as byte } as byte[])
        }
    }
}
