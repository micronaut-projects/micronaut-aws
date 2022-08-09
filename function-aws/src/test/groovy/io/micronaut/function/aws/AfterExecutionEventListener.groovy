package io.micronaut.function.aws

import groovy.transform.CompileStatic
import io.micronaut.context.event.ApplicationEventListener
import io.micronaut.function.aws.event.AfterExecutionEvent
import jakarta.inject.Singleton

@Singleton
@CompileStatic
class AfterExecutionEventListener implements ApplicationEventListener<AfterExecutionEvent> {

    AfterExecutionEvent lastEvent

    @Override
    void onApplicationEvent(AfterExecutionEvent event) {
        lastEvent = event
    }

}
