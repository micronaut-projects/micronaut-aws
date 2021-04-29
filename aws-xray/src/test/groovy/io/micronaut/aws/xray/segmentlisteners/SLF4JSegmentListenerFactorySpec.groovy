package io.micronaut.aws.xray.segmentlisteners

import com.amazonaws.xray.listeners.SegmentListener
import com.amazonaws.xray.slf4j.SLF4JSegmentListener
import io.micronaut.aws.xray.ApplicationContextSpecification

class SLF4JSegmentListenerFactorySpec extends ApplicationContextSpecification {

    void 'SLF4JSegmentListener exists as a bean of type SegmentListener if aws-xray-recorder-sdk-slf4j dependency exists'() {
        expect:
        applicationContext.getBeansOfType(SegmentListener)
        applicationContext.getBeansOfType(SegmentListener).stream()
                .anyMatch(segmentListener -> segmentListener instanceof SLF4JSegmentListener)
    }
}
