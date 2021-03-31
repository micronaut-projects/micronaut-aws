package io.micronaut.tracing.aws.annotation

import com.amazonaws.xray.AWSXRay
import com.amazonaws.xray.exceptions.SegmentNotFoundException
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.tracing.aws.SegmentBean
import io.micronaut.tracing.aws.TestEmitter
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

class AwsXraySegmentInterceptorSpec extends Specification {

    @AutoCleanup
    @Shared
    ApplicationContext applicationContext = ApplicationContext.run(Environment.AMAZON_EC2)

    @Shared
    TestEmitter testEmitter = applicationContext.getBean(TestEmitter)

    @Shared
    SegmentBean segmentBean = applicationContext.getBean(SegmentBean)

    def setup() {
        testEmitter.reset()
    }

    def "it configures the segment namespace"() {
        when:
        segmentBean.customSegmentWithNamespace()

        then:
        testEmitter.segments
        testEmitter.segments[0].name == "bar"
        testEmitter.segments[0].namespace == "namespace"
    }

    def "it configures the segment name"() {
        when:
        segmentBean.methodName()

        then:
        !testEmitter.getSegments().isEmpty()
        testEmitter.segments[0].name == "methodName"
        testEmitter.reset()

        when:
        segmentBean.customSegment()

        then:
        !testEmitter.getSegments().isEmpty()
        testEmitter.segments[0].name == "bar"
    }

    def "it creates new segment with metadata data of existing segment"() {
        when:
        def parentSegment = AWSXRay.beginSegment("parent segment")
        segmentBean.customSegmentWithNamespace()
        parentSegment.run(() -> AWSXRay.globalRecorder.endSegment(), AWSXRay.globalRecorder)

        then:
        testEmitter.segments
        testEmitter.segments.size() == 2
        testEmitter.segments[0].name == "bar"
        testEmitter.segments[0].namespace == "namespace"
        testEmitter.segments[1].name == "parent segment"
        testEmitter.segments[0].traceId == testEmitter.segments[1].traceId
    }

    def "it throws exception when there is no segment"() {
        when:
        segmentBean.subsegment()

        then:
        thrown(SegmentNotFoundException)
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
        testEmitter.reset()
    }
}
