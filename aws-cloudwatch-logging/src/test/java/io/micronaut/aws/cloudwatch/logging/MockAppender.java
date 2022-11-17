package io.micronaut.aws.cloudwatch.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.AppenderBase;

import java.util.ArrayList;
import java.util.List;

public class MockAppender extends AppenderBase<ILoggingEvent> {

    private static final List<ILoggingEvent> events = new ArrayList<>();

    static List<ILoggingEvent> getEvents() {
        synchronized (events) {
            return new ArrayList<>(events);
        }
    }

    @Override
    public String getName() {
        return "MockAppender";
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        synchronized (events) {
            events.add(eventObject);
        }
    }
}
