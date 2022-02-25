package io.micronaut.aws.xray.filters.server

import com.amazonaws.xray.AWSXRayRecorderBuilder
import com.amazonaws.xray.emitters.Emitter
import com.amazonaws.xray.entities.Segment
import com.amazonaws.xray.entities.Subsegment
import io.micronaut.aws.xray.TestEmitter
import io.micronaut.aws.xray.TestEmitterXRayRecorderBuilderBeanListener
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import io.micronaut.http.HttpResponse
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import spock.lang.Specification
import jakarta.inject.Singleton

class XRayHttpServerFilterSpec extends Specification {

    void "it creates segment with resolved host as segment name"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "tracing.xray.client-filter": "false",
                "spec.name": "XRayHttpServerFilterSpec"
        ])
        ApplicationContext context = embeddedServer.applicationContext
        TestEmitter emitter = context.getBean(TestEmitter.class)
        HttpClient client = context.createBean(HttpClient, embeddedServer.URL)

        when:
        HttpResponse<String> response = client.toBlocking().exchange("/success", String)

        then:
        noExceptionThrown()
        response
        response.code() == 200
        response.body() == "pong"
        !emitter.segments.isEmpty()
        emitter.segments[0].name.startsWith('http://localhost')

        cleanup:
        client.close()
        embeddedServer.close()
    }

    void "it creates segment with micronaut application segment name"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "tracing.xray.client-filter" : "false",
                "spec.name": "XRayHttpServerFilterSpec",
                "tracing.xray.fixed-name": 'micronautapp',
        ])
        ApplicationContext context = embeddedServer.getApplicationContext()
        TestEmitter emitter = context.getBean(TestEmitter.class)
        HttpClient client = context.createBean(HttpClient, embeddedServer.URL)

        when:
        client.toBlocking().exchange("/success")

        then:
        noExceptionThrown()
        emitter.segments
        emitter.segments[0].name == 'micronautapp'

        cleanup:
        client.close()
        embeddedServer.close()
    }

    def "it configures the exception in segment when throwed"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "tracing.xray.client-filter": "false",
                "spec.name": "XRayHttpServerFilterSpec"
        ])
        ApplicationContext context = embeddedServer.getApplicationContext()
        TestEmitter emitter = context.getBean(TestEmitter.class)
        HttpClient client = context.createBean(HttpClient, embeddedServer.URL)

        when:
        client.toBlocking().exchange("/fail", String)

        then:
        thrown(HttpClientResponseException)
        emitter.segments
        emitter.segments[0].isError()

        cleanup:
        client.close()
        embeddedServer.close()
    }

    def "it handles flowables"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "tracing.xray.client-filter.enabled": "false",
                "spec.name": "XRayHttpServerFilterSpec"
        ])
        ApplicationContext context = embeddedServer.getApplicationContext()
        TestEmitter emitter = context.getBean(TestEmitter.class)
        HttpClient client = context.createBean(HttpClient, embeddedServer.URL)

        when:
        HttpResponse<String> response = client.toBlocking().exchange("/flowable", String)

        then:
        noExceptionThrown()
        response
        response.code() == 200
        response.body() == "flowable"
        emitter.segments
        emitter.segments[0].name.startsWith('http://localhost:')

        cleanup:

        client.close()
        embeddedServer.close()
    }

    @Requires(property = "spec.name", value = "XRayHttpServerFilterSpec")
    @Controller
    @Secured(SecurityRule.IS_ANONYMOUS)
    static class TestController {

        @Get(uri = "/success", processes = MediaType.TEXT_PLAIN)
        String success() {
            "pong"
        }

        @Get(uri = "/fail", processes = MediaType.TEXT_PLAIN)
        String fail() {
            throw new NullPointerException("Failed")
        }

        @Get(uri = "/flowable", processes = MediaType.TEXT_PLAIN)
        Publisher<String> flowable() {
            Flux.just("flowable")
        }

    }

    @Requires(property = 'spec.name', value = 'XRayHttpServerFilterSpec')
    @Singleton
    static class MockXRayRecorderBuilderBeanListener extends TestEmitterXRayRecorderBuilderBeanListener {
        MockXRayRecorderBuilderBeanListener(TestEmitter emitter) {
            super(emitter)
        }
    }

    @Requires(property = 'spec.name', value = 'XRayHttpServerFilterSpec')
    @Singleton
    static class MockTestEmitter extends TestEmitter {

    }

}
