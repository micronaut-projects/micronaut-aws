package io.micronaut.aws.xray

import com.amazonaws.xray.AWSXRay
import com.amazonaws.xray.AWSXRayRecorder
import com.amazonaws.xray.AWSXRayRecorderBuilder
import io.micronaut.context.ApplicationContext
import io.micronaut.context.env.Environment
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import spock.lang.Shared
import spock.lang.Specification

import javax.inject.Inject

@MicronautTest(environments = Environment.AMAZON_EC2)
class XRayRecorderFactorySpec extends Specification {

    @Inject
    @Shared
    ApplicationContext context

    def "it creates recorder bean"(){
        expect:
        context.containsBean(AWSXRayRecorder.class)
        context.containsBean(AWSXRayRecorderBuilder.class)
        context.getBean(AWSXRayRecorder) == AWSXRay.getGlobalRecorder()
    }
}
