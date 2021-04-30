package io.micronaut.aws.xray.annotation

import com.amazonaws.xray.AWSXRay
import com.amazonaws.xray.AWSXRayRecorderBuilder
import com.amazonaws.xray.emitters.Emitter
import com.amazonaws.xray.entities.Segment
import com.amazonaws.xray.entities.Subsegment
import com.amazonaws.xray.exceptions.SegmentNotFoundException
import io.micronaut.aws.xray.ApplicationContextSpecification

import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import io.micronaut.core.annotation.Nullable
import spock.lang.Shared

import javax.inject.Singleton

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

    @Requires(property = 'spec.name', value = 'AwsXraySubsegmentInterceptorSpec')
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
