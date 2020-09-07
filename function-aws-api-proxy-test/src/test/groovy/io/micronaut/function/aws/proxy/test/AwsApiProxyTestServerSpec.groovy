package io.micronaut.function.aws.proxy.test

import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Body
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.annotation.Post
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.test.annotation.MicronautTest
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest
class AwsApiProxyTestServerSpec extends Specification {
    @Inject
    @Client('/')
    RxHttpClient client

    void 'test invoke function via server'() {
        when:
        def result = client.retrieve('/test').blockingFirst()

        then:
        result == 'good'
    }


    void 'test invoke function via server 2'() {
        when:
        def result = client.retrieve('/test').blockingFirst()

        then:
        result == 'good'
    }

    void 'test invoke post via server'() {
        when:
        def result = client.retrieve(HttpRequest.POST('/test', "body")
                                        .contentType(MediaType.TEXT_PLAIN), String).blockingFirst()

        then:
        result == 'goodbody'
    }


    @Controller('/test')
    static class TestController {
        @Get(value = '/', produces = MediaType.TEXT_PLAIN)
        String test() {
            return 'good'
        }

        @Post(value = '/', processes = MediaType.TEXT_PLAIN)
        String test(@Body String body) {
            return 'good' + body
        }
    }
}
