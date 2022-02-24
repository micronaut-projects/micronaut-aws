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
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import io.micronaut.core.annotation.Nullable
import spock.lang.Shared

import jakarta.inject.Singleton

class AwsXraySubsegmentInterceptorSpec extends ApplicationContextSpecification {

    @Override
    @Nullable
    String getSpecName() {
        'AwsXraySubsegmentInterceptorSpec'
    }

    @Shared
    TestEmitter testEmitter = applicationContext.getBean(TestEmitter)

    @Shared
    SegmentBean segmentBean = applicationContext.getBean(SegmentBean)

    def cleanup() {
        testEmitter.reset()
    }

    def "if no segment found no subsegment is created and no exception is thrown"() {
        when:
        segmentBean.subsegment()

        then:
        noExceptionThrown()
        testEmitter.segments.isEmpty()
        testEmitter.subsegments.isEmpty()
    }

    def "it configures the subsegment name"() {
        when:
        AWSXRay.createSegment("segment 1", () -> segmentBean.subsegment())

        then:
        testEmitter.segments
        testEmitter.segments[0].name == "segment 1"
        testEmitter.segments[0].subsegments
        testEmitter.segments[0].subsegments[0].name == "subsegment"
        testEmitter.reset()

        when:
        AWSXRay.createSegment("segment 2", () -> segmentBean.customSubsegment())

        then:
        testEmitter.segments
        testEmitter.segments[0].name == "segment 2"
        testEmitter.segments[0].subsegments
        testEmitter.segments[0].subsegments[0].name == "foo"
    }

    @Requires(property = 'spec.name', value = 'AwsXraySubsegmentInterceptorSpec')
    @Singleton
    static class SegmentBean {
        @AwsXraySubsegment
        String subsegment() {
            return "method name as subsegment name";
        }

        @AwsXraySubsegment(name = "foo")
        String customSubsegment() {
            return "method name as subsegment name";
        }
    }

    @Requires(property = 'spec.name', value = 'AwsXraySubsegmentInterceptorSpec')
    @Singleton
    static class MockTestEmitterXRayRecorderBuilderBeanListener extends TestEmitterXRayRecorderBuilderBeanListener {

        MockTestEmitterXRayRecorderBuilderBeanListener(TestEmitter emitter) {
            super(emitter)
        }
    }

    @Requires(property = 'spec.name', value = 'AwsXraySubsegmentInterceptorSpec')
    @Singleton
    static class MockTestEmitter extends TestEmitter {

    }
}
