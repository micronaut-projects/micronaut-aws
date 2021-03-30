package io.micronaut.tracing.aws.filter

import com.amazonaws.xray.AWSXRay
import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.RxHttpClient
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.scheduling.TaskExecutors
import io.micronaut.scheduling.annotation.ExecuteOn
import io.micronaut.tracing.aws.TestEmitter
import io.reactivex.Flowable
import spock.lang.Specification

class XRayHttpClientFilterSpec extends Specification {

    def "it creates subsegment when segment configured"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "micronaut.application.name"        : "test-application",
                "aws.xray.httpfilter.server.enabled": "false",
                "specName"                          : "XRayHttpClientFilterSpec"
        ], Environment.AMAZON_EC2)
        ApplicationContext context = embeddedServer.getApplicationContext()
        TestEmitter emitter = context.getBean(TestEmitter.class)
        RxHttpClient client = context.createBean(RxHttpClient)

        when:
        AWSXRay.beginSegment("test-segment")
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
        emitter.segments[0].subsegments[0].name.startsWith("localhost")
        emitter.segments[0].subsegments[0].isEmitted()

        cleanup:
        embeddedServer.stop()
    }

    def "it creates subsegment with exception when segment configured"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "micronaut.application.name"        : "test-application",
                "aws.xray.httpfilter.server.enabled": "false",
                "specName"                          : "XRayHttpClientFilterSpec"
        ], Environment.AMAZON_EC2)
        ApplicationContext context = embeddedServer.getApplicationContext()
        TestEmitter emitter = context.getBean(TestEmitter.class)
        RxHttpClient client = context.createBean(RxHttpClient)

        when:
        try {
            AWSXRay.beginSegment("test-segment")
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
        emitter.segments[0].subsegments[0].name.startsWith("localhost")
        emitter.segments[0].subsegments[0].isFault()
        emitter.segments[0].subsegments[0].cause
        emitter.segments[0].subsegments[0].http
        emitter.segments[0].subsegments[0].cause.exceptions

        cleanup:
        embeddedServer.stop()
    }

    def "it fails to create subsegment when segment not configure"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "micronaut.application.name"        : "test-application",
                "aws.xray.httpfilter.server.enabled": "false",
                "specName"                          : "XRayHttpClientFilterSpec"
        ], Environment.AMAZON_EC2)
        ApplicationContext context = embeddedServer.getApplicationContext()
        TestEmitter emitter = context.getBean(TestEmitter.class)
        RxHttpClient client = context.createBean(RxHttpClient)

        when:
        def response = client.toBlocking().exchange("${embeddedServer.getURL()}/success", String)

        then:
        response
        response.code() == 200
        response.body() == "pong"
        !emitter.segments

        cleanup:
        embeddedServer.stop()
    }


    @Requires(property = "specName", value = "XRayHttpClientFilterSpec")
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

}
