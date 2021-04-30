package io.micronaut.aws.xray.filters.client

import com.amazonaws.xray.AWSXRay
import com.amazonaws.xray.AWSXRayRecorderBuilder
import com.amazonaws.xray.emitters.Emitter
import com.amazonaws.xray.entities.Segment
import com.amazonaws.xray.entities.Subsegment
import io.micronaut.aws.xray.filters.server.XRayHttpServerFilter
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import io.micronaut.core.convert.value.MutableConvertibleValues
import io.micronaut.core.util.StringUtils
import io.micronaut.http.HttpMethod
import io.micronaut.http.HttpRequest
import io.micronaut.http.MediaType
import io.micronaut.http.MutableHttpHeaders
import io.micronaut.http.MutableHttpParameters
import io.micronaut.http.MutableHttpRequest
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.context.ServerRequestContext
import io.micronaut.http.cookie.Cookie
import io.micronaut.http.cookie.Cookies
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.reactivex.Flowable
import spock.lang.Specification
import javax.inject.Singleton

class XRayHttpClientFilterSpec extends Specification {

    def "it creates subsegment when segment configured"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "tracing.xray.server-filter": StringUtils.FALSE,
                "spec.name"                          : "XRayHttpClientFilterSpec"
        ])
        ApplicationContext context = embeddedServer.getApplicationContext()
        TestEmitter emitter = context.getBean(TestEmitter.class)
        RxHttpClient client = context.createBean(RxHttpClient)

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

    def "it creates subsegment with exception when segment configured"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "tracing.xray.server-filter" : StringUtils.FALSE,
                "spec.name"                  : "XRayHttpClientFilterSpec"
        ])
        ApplicationContext context = embeddedServer.getApplicationContext()
        TestEmitter emitter = context.getBean(TestEmitter.class)
        RxHttpClient client = context.createBean(RxHttpClient)

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
        RxHttpClient client = context.createBean(RxHttpClient)

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
        Flowable<String> flowable() {
            return Flowable.just("flowable")
        }
    }

    @Requires(property = 'spec.name', value = 'XRayHttpClientFilterSpec')
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

    @Requires(property = 'spec.name', value = 'XRayHttpClientFilterSpec')
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
