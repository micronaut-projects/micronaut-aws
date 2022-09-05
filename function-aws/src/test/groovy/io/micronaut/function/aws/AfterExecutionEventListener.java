package io.micronaut.function.aws;

import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.function.aws.event.AfterExecutionEvent;
import jakarta.inject.Singleton;

@Singleton
public class AfterExecutionEventListener implements ApplicationEventListener<AfterExecutionEvent> {

    private AfterExecutionEvent lastEvent;

    @Override
    public void onApplicationEvent(AfterExecutionEvent event) {
        lastEvent = event;
    }

    public AfterExecutionEvent getLastEvent() {
        return lastEvent;
    }

    public void setLastEvent(AfterExecutionEvent lastEvent) {
        this.lastEvent = lastEvent;
    }

}
