package io.micronaut.aws.xray;

import com.amazonaws.xray.AWSXRayRecorderBuilder;
import io.micronaut.context.event.BeanCreatedEvent;
import io.micronaut.context.event.BeanCreatedEventListener;

import javax.inject.Singleton;

/**
 * Configuration of test emitter into {@link AWSXRayRecorderBuilder}.
 */
@Singleton
class XRayRecorderBuilderBeanListener implements BeanCreatedEventListener<AWSXRayRecorderBuilder> {

    private TestEmitter emitter;

    public XRayRecorderBuilderBeanListener(TestEmitter emitter) {
        this.emitter = emitter;
    }

    @Override
    public AWSXRayRecorderBuilder onCreated(BeanCreatedEvent<AWSXRayRecorderBuilder> event) {
        return event.getBean().withEmitter(emitter);
    }
}
