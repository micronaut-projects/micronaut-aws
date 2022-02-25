package io.micronaut.aws.xray.annotation

import com.amazonaws.xray.entities.Subsegment
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
import io.micronaut.http.client.exceptions.HttpClientResponseException
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import spock.lang.Specification

import javax.validation.constraints.NotBlank
import java.util.concurrent.CompletableFuture

@Property(name = "spec.name", value = "AwsXraySubsegmentInterceptorCompleteableExceptionSpec")
@MicronautTest
class AwsXraySubsegmentInterceptorCompleteableExceptionSpec extends Specification {
    @Inject
    @Client("/")
    HttpClient httpClient

    @Inject
    TestEmitter emitter

    void "AwsXraySubsegmentInterceptor adds the exception thrown in the method returning CompleteableFuture to the subsegment"() {
        given:
        BlockingHttpClient client = httpClient.toBlocking()
        HttpRequest<?> request = HttpRequest.GET('/message')

        when:
        client.exchange(request)

        then:
        thrown(HttpClientResponseException)
        emitter.segments
        emitter.segments.size() == 1

        when:
        def segment = emitter.segments.get(0)

        then:
        segment.subsegmentsCopy
        segment.subsegmentsCopy.size() == 1

        when:
        Subsegment subsegment = segment.subsegmentsCopy.get(0)

        then:
        'compose' == subsegment.name
        segment.isFault()
        subsegment.isFault()
        segment.cause.exceptions
        subsegment.cause.exceptions
        subsegment.cause.exceptions.get(0).type == 'java.lang.UnsupportedOperationException'
    }

    @Requires(property = 'spec.name', value = 'AwsXraySubsegmentInterceptorCompleteableExceptionSpec')
    @Singleton
    static class MockTestEmitterXRayRecorderBuilderBeanListener extends TestEmitterXRayRecorderBuilderBeanListener {
        MockTestEmitterXRayRecorderBuilderBeanListener(TestEmitter emitter) {
            super(emitter)
        }
    }

    @Requires(property = 'spec.name', value = 'AwsXraySubsegmentInterceptorCompleteableExceptionSpec')
    @Singleton
    static class MockTestEmitter extends TestEmitter {
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


    @Requires(property = "spec.name", value = "AwsXraySubsegmentInterceptorCompleteableExceptionSpec")
    @Controller("/message")
    static class MockController {
        private final MessageComposer messageComposer
        MockController(MessageComposer messageComposer) {
            this.messageComposer = messageComposer
        }
        @Secured(SecurityRule.IS_ANONYMOUS)
        @Get
        @SingleResult
        CompletableFuture<Message> index() {
            messageComposer.compose()
        }
    }

    static interface MessageComposer {
        CompletableFuture<Message> compose();
    }

    @Requires(property = "spec.name", value = "AwsXraySubsegmentInterceptorCompleteableExceptionSpec")
    @Singleton
    static class MessageComposerImpl implements MessageComposer {

        @AwsXraySubsegment
        @Override
        @SingleResult
        CompletableFuture<Message> compose() {
            CompletableFuture.supplyAsync(() -> {
                throw new UnsupportedOperationException()
            })
        }
    }
}
