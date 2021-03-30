package io.micronaut.tracing.aws.annotation

import com.amazonaws.xray.AWSXRay
import com.amazonaws.xray.AWSXRayRecorderBuilder
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
        AWSXRay.setGlobalRecorder(AWSXRayRecorderBuilder.standard().withEmitter(testEmitter).build())
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

    def "it skips subsegment when there is no segment"() {
        when:
        segmentBean.subsegment()

        then:
        testEmitter.segments.isEmpty()
        testEmitter.subsegments.isEmpty()
    }

    def "it configures the subsegment name"() {
        when:
        AWSXRay.beginSegment("segment 1")
        segmentBean.subsegment()
        AWSXRay.endSegment()

        then:
        testEmitter.segments
        testEmitter.segments[0].name == "segment 1"
        testEmitter.segments[0].subsegments
        testEmitter.segments[0].subsegments[0].name == "subsegment"
        testEmitter.reset()

        when:
        AWSXRay.beginSegment("segment 2")
        segmentBean.customSubsegment()
        AWSXRay.endSegment()

        then:
        testEmitter.segments
        testEmitter.segments[0].name == "segment 2"
        testEmitter.segments[0].subsegments
        testEmitter.segments[0].subsegments[0].name == "foo"
        testEmitter.reset()
    }
}
