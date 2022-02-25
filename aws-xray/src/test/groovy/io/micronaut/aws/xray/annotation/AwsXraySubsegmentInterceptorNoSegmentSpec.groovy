package io.micronaut.aws.xray.annotation

import com.amazonaws.xray.AWSXRay
import com.amazonaws.xray.AWSXRayRecorderBuilder
import com.amazonaws.xray.emitters.Emitter
import com.amazonaws.xray.entities.Segment
import com.amazonaws.xray.entities.Subsegment
import com.amazonaws.xray.exceptions.SegmentNotFoundException
import io.micronaut.aws.xray.ApplicationContextSpecification
import io.micronaut.aws.xray.TestEmitter
import io.micronaut.aws.xray.TestEmitterXRayRecorderBuilderBeanListener
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import io.micronaut.core.annotation.NonNull
import io.micronaut.core.annotation.Nullable
import io.micronaut.http.annotation.Controller
import io.micronaut.http.annotation.Get
import io.micronaut.security.annotation.Secured
import io.micronaut.security.rules.SecurityRule
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Ignore
import spock.lang.Shared

import jakarta.inject.Singleton
import spock.lang.Specification

import javax.validation.constraints.NotBlank

@Property(name = "spec.name", value = "AwsXraySubsegmentInterceptorNoSegmentSpec")
@MicronautTest(startApplication = false)
class AwsXraySubsegmentInterceptorNoSegmentSpec extends Specification {

    @Inject
    TestEmitter testEmitter

    @Inject
    MessageComposer messageComposer

    def cleanup() {
        testEmitter.reset()
    }

    def "if no segment found no subsegment is created and no exception is thrown"() {
        when:
        messageComposer.compose()

        then:
        noExceptionThrown()
        testEmitter.segments.isEmpty()
        testEmitter.subsegments.isEmpty()
    }

    @Requires(property = 'spec.name', value = 'AwsXraySubsegmentInterceptorNoSegmentSpec')
    @Singleton
    static class MockTestEmitterXRayRecorderBuilderBeanListener extends TestEmitterXRayRecorderBuilderBeanListener {
        MockTestEmitterXRayRecorderBuilderBeanListener(TestEmitter emitter) {
            super(emitter)
        }
    }

    @Requires(property = 'spec.name', value = 'AwsXraySubsegmentInterceptorNoSegmentSpec')
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

    static interface MessageComposer {
        Message compose();
    }

    @Requires(property = "spec.name", value = "AwsXraySubsegmentInterceptorNoSegmentSpec")
    @Singleton
    static class MessageComposerImpl implements MessageComposer {

        @AwsXraySubsegment
        @Override
        Message compose() {
            return new Message("Hello World");
        }
    }
}
