package io.micronaut.aws.xray

import com.amazonaws.xray.AWSXRayRecorderBuilder
import groovy.transform.CompileStatic
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener

@CompileStatic
class TestEmitterXRayRecorderBuilderBeanListener implements BeanCreatedEventListener<AWSXRayRecorderBuilder> {

    private final TestEmitter emitter

    TestEmitterXRayRecorderBuilderBeanListener(TestEmitter emitter) {
        this.emitter = emitter
    }

    @Override
    AWSXRayRecorderBuilder onCreated(BeanCreatedEvent<AWSXRayRecorderBuilder> event) {
        event.bean.withEmitter(emitter)
    }
}
