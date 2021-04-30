package io.micronaut.aws.xray.filters.server

import com.amazonaws.xray.AWSXRayRecorderBuilder
import com.amazonaws.xray.emitters.Emitter
import com.amazonaws.xray.entities.Segment
import com.amazonaws.xray.entities.Subsegment
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import io.reactivex.Flowable
import spock.lang.Specification
import javax.inject.Singleton

class XRayHttpServerFilterSpec extends Specification {

    def "it creates segment with resolved host as segment name"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "tracing.xray.client-filter": "false",
                "spec.name": "XRayHttpServerFilterSpec"
        ])
        ApplicationContext context = embeddedServer.getApplicationContext()
        TestEmitter emitter = context.getBean(TestEmitter.class)
        RxHttpClient client = context.createBean(RxHttpClient)

        when:
        def response = client.toBlocking().exchange("${embeddedServer.getURL()}/success", String)

        then:
        response
        response.code() == 200
        response.body() == "pong"
        !emitter.segments.isEmpty()
        emitter.segments[0].name.startsWith('http://localhost')

        cleanup:
        embeddedServer.stop()
    }

    def "it creates segment with micronaut application segment name"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "tracing.xray.client-filter" : "false",
                "spec.name": "XRayHttpServerFilterSpec",
                "tracing.xray.fixed-name": 'micronautapp',
        ])
        ApplicationContext context = embeddedServer.getApplicationContext()
        TestEmitter emitter = context.getBean(TestEmitter.class)
        RxHttpClient client = context.createBean(RxHttpClient)

        when:
        client.toBlocking().exchange("${embeddedServer.getURL()}/success")

        then:
        emitter.segments
        emitter.segments[0].name == 'micronautapp'

        cleanup:
        embeddedServer.stop()
    }

    def "it configures the exception in segment when throwed"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "tracing.xray.client-filter": "false",
                "spec.name": "XRayHttpServerFilterSpec"
        ])
        ApplicationContext context = embeddedServer.getApplicationContext()
        TestEmitter emitter = context.getBean(TestEmitter.class)
        RxHttpClient client = context.createBean(RxHttpClient)

        when:
        client.toBlocking().exchange("${embeddedServer.getURL()}/fail", String)

        then:
        thrown(HttpClientResponseException)
        emitter.segments
        emitter.segments[0].isFault()

        cleanup:
        embeddedServer.stop()
    }

    def "it handles flowables"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "tracing.xray.client-filter.enabled": "false",
                "spec.name": "XRayHttpServerFilterSpec"
        ])
        ApplicationContext context = embeddedServer.getApplicationContext()
        TestEmitter emitter = context.getBean(TestEmitter.class)
        RxHttpClient client = context.createBean(RxHttpClient)

        when:
        def response = client.toBlocking().exchange("${embeddedServer.getURL()}/flowable", String)

        then:
        response
        response.code() == 200
        response.body() == "flowable"
        emitter.segments
        emitter.segments[0].name.startsWith('http://localhost:')

        cleanup:
        embeddedServer.stop()
    }

    @Requires(property = "spec.name", value = "XRayHttpServerFilterSpec")
    @Controller
    static class TestController {

        @Get(uri = "/success", processes = MediaType.TEXT_PLAIN)
        String success() {
            return "pong"
        }

        @Get(uri = "/fail", processes = MediaType.TEXT_PLAIN)
        String fail() {
            throw new NullPointerException("Failed")
        }

        @Get(uri = "/flowable", processes = MediaType.TEXT_PLAIN)
        Flowable<String> flowable() {
            return Flowable.just("flowable")
        }

    }

    @Requires(property = 'spec.name', value = 'XRayHttpServerFilterSpec')
    @Singleton
    static class XRayRecorderBuilderBeanListener implements BeanCreatedEventListener<AWSXRayRecorderBuilder> {

        private final TestEmitter emitter

        XRayRecorderBuilderBeanListener(TestEmitter emitter) {
            this.emitter = emitter
        }

        @Override
        AWSXRayRecorderBuilder onCreated(BeanCreatedEvent<AWSXRayRecorderBuilder> event) {
            event.bean.withEmitter(emitter)
        }
    }

    @Requires(property = 'spec.name', value = 'XRayHttpServerFilterSpec')
    @Singleton
    static class TestEmitter extends Emitter {

        List<Segment> segments = []
        List<Subsegment> subsegments = []

        @Override
        boolean sendSegment(Segment segment) {
            segments.add(segment)
            true
        }

        @Override
        boolean sendSubsegment(Subsegment subsegment) {
            subsegments.add(subsegment)
            true
        }

        void reset() {
            segments.clear()
            subsegments.clear()
        }
    }

}
