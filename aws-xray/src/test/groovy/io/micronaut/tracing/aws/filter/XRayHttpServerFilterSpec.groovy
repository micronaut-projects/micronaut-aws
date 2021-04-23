package io.micronaut.tracing.aws.filter


import io.micronaut.context.ApplicationContext
import io.micronaut.context.annotation.Requires
import io.micronaut.context.env.Environment
import io.micronaut.http.MediaType
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.RxHttpClient
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.tracing.aws.TestEmitter
import io.reactivex.Flowable
import spock.lang.Specification

class XRayHttpServerFilterSpec extends Specification {

    def "it creates segment with fallback segment name"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "aws.xray.http-filter.client.enabled": "false",
                "spec.name": "XRayHttpServerFilterSpec"
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
        emitter.segments
        emitter.segments[0].name == 'micronaut.xray-http-filter'

        cleanup:
        embeddedServer.stop()
    }

    def "it creates segment with micronaut application segment name"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "micronaut.application.name"        : "test-application",
                "aws.xray.http-filter.client.enabled": "false",
                "spec.name": "XRayHttpServerFilterSpec"
        ], Environment.AMAZON_EC2)
        ApplicationContext context = embeddedServer.getApplicationContext()
        TestEmitter emitter = context.getBean(TestEmitter.class)
        RxHttpClient client = context.createBean(RxHttpClient)

        when:
        client.toBlocking().exchange("${embeddedServer.getURL()}/success")

        then:
        emitter.segments
        emitter.segments[0].name == 'test-application'

        cleanup:
        embeddedServer.stop()
    }

    def "it creates segment with configured fixed segment name"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "micronaut.application.name"                 : "test-application",
                "aws.xray.http-filter.server.fixed-segment-name": "fixed-segment-name",
                "aws.xray.http-filter.client.enabled"         : "false",
                "spec.name": "XRayHttpServerFilterSpec"
        ], Environment.AMAZON_EC2)
        ApplicationContext context = embeddedServer.getApplicationContext()
        TestEmitter emitter = context.getBean(TestEmitter.class)
        RxHttpClient client = context.createBean(RxHttpClient)

        when:
        client.toBlocking().exchange("${embeddedServer.getURL()}/success")

        then:
        emitter.segments
        emitter.segments[0].name == 'fixed-segment-name'

        cleanup:
        embeddedServer.stop()
    }

    def "it configures the exception in segment when throwed"() {
        given:
        EmbeddedServer embeddedServer = ApplicationContext.run(EmbeddedServer.class, [
                "micronaut.application.name"        : "test-application",
                "aws.xray.http-filter.client.enabled": "false",
                "spec.name": "XRayHttpServerFilterSpec"
        ], Environment.AMAZON_EC2)
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
                "micronaut.application.name"        : "test-application",
                "aws.xray.http-filter.client.enabled": "false",
                "spec.name": "XRayHttpServerFilterSpec"
        ], Environment.AMAZON_EC2)
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
        emitter.segments[0].name == 'test-application'

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

}
