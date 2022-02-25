package io.micronaut.aws.xray.annotation


import io.micronaut.aws.xray.TestEmitter
import io.micronaut.aws.xray.TestEmitterXRayRecorderBuilderBeanListener
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.core.annotation.NonNull
import io.micronaut.core.async.annotation.SingleResult
import io.micronaut.http.HttpRequest
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.http.client.BlockingHttpClient
import io.micronaut.http.client.HttpClient
import io.micronaut.http.client.annotation.Client
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import org.reactivestreams.Publisher
import reactor.core.publisher.Mono
import spock.lang.Specification

import javax.validation.constraints.NotBlank

@Property(name = "spec.name", value = "AwsXraySubsegmentInterceptorPublisherSpec")
@MicronautTest
class AwsXraySubsegmentInterceptorPublisherSpec extends Specification {
    @Inject
    @Client("/")
    HttpClient httpClient

    @Inject
    TestEmitter emitter

    void "AwsXraySubsegment annotation creates a subsegment with the intercepted method name in a method which returns Publisher"() {
        given:
        BlockingHttpClient client = httpClient.toBlocking()
        HttpRequest<?> request = HttpRequest.GET('/message')

        when:
        client.exchange(request)

        then:
        noExceptionThrown()
        emitter.segments
        emitter.segments.size() == 1
        emitter.segments.get(0).subsegmentsCopy
        emitter.segments.get(0).subsegmentsCopy.size() == 1
        'compose' == emitter.segments.get(0).subsegmentsCopy.get(0).name
    }

    @Requires(property = 'spec.name', value = 'AwsXraySubsegmentInterceptorPublisherSpec')
    @Singleton
    static class MockTestEmitterXRayRecorderBuilderBeanListener extends TestEmitterXRayRecorderBuilderBeanListener {
        MockTestEmitterXRayRecorderBuilderBeanListener(TestEmitter emitter) {
            super(emitter)
        }
    }

    @Requires(property = 'spec.name', value = 'AwsXraySubsegmentInterceptorPublisherSpec')
    @Singleton
    static class MockTestEmitter extends TestEmitter {
    }


    @Requires(property = "spec.name", value = "AwsXraySubsegmentInterceptorPublisherSpec")
    @Controller("/message")
    static class MockController {
        private final MessageComposer messageComposer
        MockController(MessageComposer messageComposer) {
            this.messageComposer = messageComposer
        }
        @Secured(SecurityRule.IS_ANONYMOUS)
        @Get
        @SingleResult
        Publisher<Message> index() {
            messageComposer.compose()
        }
    }

    static class Message {
        @NonNull
        @NotBlank
        private final String message;

        Message(@NonNull String message) {
            this.message = message;
        }

        @NonNull
        String getMessage() {
            return message
        }
    }

    static interface MessageComposer {
        @SingleResult
        Publisher<Message> compose();
    }

    @Requires(property = "spec.name", value = "AwsXraySubsegmentInterceptorPublisherSpec")
    @Singleton
    static class MessageComposerImpl implements MessageComposer {

        @AwsXraySubsegment
        @Override
        @SingleResult
        Publisher<Message> compose() {
            return Mono.just(new Message("Hello World"))
        }
    }
}
