package io.micronaut.aws.xray.filters.client

import com.amazonaws.xray.AWSXRay
import com.amazonaws.xray.AWSXRayRecorderBuilder
import com.amazonaws.xray.emitters.Emitter
import com.amazonaws.xray.entities.Segment
import com.amazonaws.xray.entities.Subsegment
import io.micronaut.aws.xray.TestEmitter
import io.micronaut.aws.xray.TestEmitterXRayRecorderBuilderBeanListener
import io.micronaut.aws.xray.filters.server.XRayHttpServerFilter
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import io.micronaut.core.util.StringUtils
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.HttpClient
import io.micronaut.http.context.ServerRequestContext
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import reactor.core.publisher.Flux
import spock.lang.Specification

class XRayHttpClientFilterSpec extends Specification {

    def "it creates subsegment when segment configured"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "tracing.xray.server-filter": StringUtils.FALSE,
                "spec.name"                          : "XRayHttpClientFilterSpec"
        ])
        ApplicationContext context = embeddedServer.getApplicationContext()
        TestEmitter emitter = context.getBean(TestEmitter.class)
        HttpClient client = context.createBean(HttpClient)

        expect:
        !context.containsBean(XRayHttpServerFilter)

        when:
        AWSXRay.beginSegment("test-segment")
        def httpRequest = Stub(HttpRequest) {
            getAttribute(_, _) >> Optional.of(AWSXRay.getTraceEntity())
        }
        ServerRequestContext.set(httpRequest)
        def response = client.toBlocking().exchange("${embeddedServer.getURL()}/success", String)
        AWSXRay.endSegment()

        then:
        response
        response.code() == 200
        response.body() == "pong"
        emitter.segments
        emitter.segments.size() == 1
        emitter.segments[0].name == "test-segment"
        emitter.segments[0].subsegments
        emitter.segments[0].subsegments.size() == 1
        emitter.segments[0].subsegments[0].name.startsWith("http://localhost")

        cleanup:
        emitter.reset()
        embeddedServer.close()
    }

    void "it creates subsegment with exception when segment configured"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "tracing.xray.server-filter" : StringUtils.FALSE,
                "spec.name"                  : "XRayHttpClientFilterSpec"
        ])
        ApplicationContext context = embeddedServer.getApplicationContext()
        TestEmitter emitter = context.getBean(TestEmitter.class)
        HttpClient client = context.createBean(HttpClient)

        expect:
        !context.containsBean(XRayHttpServerFilter)

        when:
        try {
            AWSXRay.beginSegment("test-segment")
            def httpRequest = Stub(HttpRequest) {
                getAttribute(_, _) >> Optional.of(AWSXRay.getTraceEntity())
            }
            ServerRequestContext.set(httpRequest)
            client.toBlocking().exchange("${embeddedServer.getURL()}/fail", String)
        } catch (Exception e) {
            // no-op
        } finally {
            AWSXRay.endSegment()
        }

        then:
        emitter.segments
        emitter.segments.size() == 1
        emitter.segments[0].name == "test-segment"
        emitter.segments[0].subsegments
        emitter.segments[0].subsegments.size() == 1
        emitter.segments[0].subsegments[0].name.startsWith("http://localhost")
        emitter.segments[0].subsegments[0].isFault()
        emitter.segments[0].subsegments[0].cause
        emitter.segments[0].subsegments[0].http
        emitter.segments[0].subsegments[0].cause.exceptions

        cleanup:
        emitter.reset()
        embeddedServer.close()
    }

    def "it fails to create subsegment when segment not configure"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "tracing.xray.server-filter": StringUtils.FALSE,
                "spec.name"                         : "XRayHttpClientFilterSpec"
        ])
        ApplicationContext context = embeddedServer.getApplicationContext()
        TestEmitter emitter = context.getBean(TestEmitter.class)
        HttpClient client = context.createBean(HttpClient)

        expect:
        !context.containsBean(XRayHttpServerFilter)

        when:
        def response = client.toBlocking().exchange("${embeddedServer.getURL()}/success", String)

        then:
        response
        response.code() == 200
        response.body() == "pong"
        !emitter.segments

        cleanup:
        emitter.reset()
        embeddedServer.close()
    }

    @Requires(property = "spec.name", value = "XRayHttpClientFilterSpec")
    @Controller
    @ExecuteOn(TaskExecutors.IO)
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
        Publisher<String> flowable() {
            return Flux.just("flowable")
        }
    }

    @Requires(property = 'spec.name', value = 'XRayHttpClientFilterSpec')
    @Singleton
    static class MockTestEmitterXRayRecorderBuilderBeanListener extends TestEmitterXRayRecorderBuilderBeanListener {

        MockTestEmitterXRayRecorderBuilderBeanListener(TestEmitter emitter) {
            super(emitter)
        }
    }

    @Requires(property = 'spec.name', value = 'XRayHttpClientFilterSpec')
    @Singleton
    static class MockTestEmitter extends TestEmitter {

    }
}
