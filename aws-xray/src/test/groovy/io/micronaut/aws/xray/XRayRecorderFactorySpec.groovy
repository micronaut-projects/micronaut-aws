package io.micronaut.aws.xray

import com.amazonaws.xray.AWSXRay
import com.amazonaws.xray.AWSXRayRecorder
import com.amazonaws.xray.AWSXRayRecorderBuilder

class XRayRecorderFactorySpec extends ApplicationContextSpecification {

    def "AWSRayRecorder bean sets itself as Global Recorder"(){
        expect:
        applicationContext.containsBean(AWSXRayRecorder)
        applicationContext.containsBean(AWSXRayRecorderBuilder)
        applicationContext.getBean(AWSXRayRecorder) == AWSXRay.getGlobalRecorder()
    }
}
