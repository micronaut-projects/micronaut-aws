package io.micronaut.aws.xray

import com.amazonaws.xray.AWSXRayRecorderBuilder
import com.amazonaws.xray.strategy.LogErrorContextMissingStrategy
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.BeanCreatedEvent
import io.micronaut.context.event.BeanCreatedEventListener
import jakarta.inject.Singleton

@Requires(property = 'spec.name', value = 'XrayRecorderBuilderContextMissingStrategySpec')
//tag::clazz[]
@Singleton
class XRayRecorderBuilderBeanListener implements BeanCreatedEventListener<AWSXRayRecorderBuilder> {

    @Override
    AWSXRayRecorderBuilder onCreated(BeanCreatedEvent<AWSXRayRecorderBuilder> event) {
        return event.getBean().withContextMissingStrategy(new LogErrorContextMissingStrategy());
    }
}
//end::clazz[]