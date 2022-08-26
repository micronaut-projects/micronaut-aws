/*
 * Copyright 2017-2022 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.aws.cloudwatch.logging;

import ch.qos.logback.classic.spi.ILoggingEvent;
import ch.qos.logback.core.Appender;
import ch.qos.logback.core.AppenderBase;
import ch.qos.logback.core.encoder.Encoder;
import ch.qos.logback.core.net.QueueFactory;
import ch.qos.logback.core.spi.AppenderAttachable;
import ch.qos.logback.core.util.Duration;
import io.micronaut.core.annotation.Internal;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.InvalidSequenceTokenException;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.ResourceAlreadyExistsException;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingDeque;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Cloudwatch log appender for logback.
 *
 * @author Nemanja Mikic
 * @since 3.8.0
 */
@Internal
public final class CloudWatchLoggingAppender extends AppenderBase<ILoggingEvent> implements AppenderAttachable<ILoggingEvent> {

    private static final int DEFAULT_QUEUE_SIZE = 128;
    private static final int DEFAULT_MAX_BATCH_SIZE = 128;
    private static final int PUT_REQUEST_RETRY_COUNT = 2;
    private static final long DEFAULT_PUBLISH_PERIOD = 100;
    private final QueueFactory queueFactory = new QueueFactory();
    private Duration eventDelayLimit;
    private final List<String> blackListLoggerName = new ArrayList<>();
    private Encoder<ILoggingEvent> encoder;
    private Future<?> task;
    private BlockingDeque<ILoggingEvent> deque;
    private int queueSize = DEFAULT_QUEUE_SIZE;
    private long publishPeriod = DEFAULT_PUBLISH_PERIOD;
    private Appender<ILoggingEvent> emergencyAppender;
    private String sequenceToken = null;
    private boolean configuredSuccessfully = false;
    private boolean createGroupAndStream = true;
    private int maxBatchSize = DEFAULT_MAX_BATCH_SIZE;
    private String groupName;
    private String streamName;

    public int getQueueSize() {
        return queueSize;
    }

    public void setQueueSize(int queueSize) {
        this.queueSize = queueSize;
    }

    public void addBlackListLoggerName(String test) {
        this.blackListLoggerName.add(test);
    }

    public String getGroupName() {
        return groupName;
    }

    public void setGroupName(String groupName) {
        this.groupName = groupName;
    }

    public String getStreamName() {
        return streamName;
    }

    public void setStreamName(String streamName) {
        this.streamName = streamName;
    }

    public long getPublishPeriod() {
        return publishPeriod;
    }

    public void setPublishPeriod(long publishPeriod) {
        this.publishPeriod = publishPeriod;
    }

    public int getMaxBatchSize() {
        return maxBatchSize;
    }

    public void setMaxBatchSize(int maxBatchSize) {
        this.maxBatchSize = maxBatchSize;
    }

    public boolean isCreateGroupAndStream() {
        return createGroupAndStream;
    }

    public void setCreateGroupAndStream(boolean createGroupAndStream) {
        this.createGroupAndStream = createGroupAndStream;
    }

    @Override
    public void start() {
        if (isStarted()) {
            return;
        }

        if (queueSize == 0) {
            addWarn("Queue size of zero is deprecated, use a size of one to indicate synchronous processing");
        }

        if (queueSize < 0) {
            addError("Queue size must be greater than zero");
            return;
        }

        if (publishPeriod <= 0) {
            addError("Publish period must be greater than zero");
            return;
        }

        if (maxBatchSize <= 0) {
            addError("Max Batch size must be greater than zero");
            return;
        }

        if (encoder == null) {
            addError("No encoder set for the appender named [" + name + "].");
            return;
        }

        if (emergencyAppender != null && !emergencyAppender.isStarted()) {
            emergencyAppender.start();
        }

        eventDelayLimit = new Duration(publishPeriod);

        deque = queueFactory.newLinkedBlockingDeque(queueSize);

        task = getContext().getScheduledExecutorService().scheduleAtFixedRate(() -> {
            try {
                dispatchEvents();
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
        }, 0, 100, TimeUnit.MILLISECONDS);
        super.start();
    }

    @Override
    public void stop() {
        if (!isStarted()) {
            return;
        }
        task.cancel(true);
        super.stop();
    }

    @Override
    protected void append(ILoggingEvent eventObject) {
        if (eventObject == null || !isStarted() || blackListLoggerName.contains(eventObject.getLoggerName())) {
            return;
        }

        try {
            final boolean inserted = deque.offer(eventObject, eventDelayLimit.getMilliseconds(), TimeUnit.MILLISECONDS);
            if (!inserted) {
                addInfo("Dropping event due to timeout limit of [" + eventDelayLimit + "] being exceeded");
            }
        } catch (InterruptedException e) {
            addError("Interrupted while appending event to SocketAppender", e);
            Thread.currentThread().interrupt();
        }
    }

    public Encoder<ILoggingEvent> getEncoder() {
        return encoder;
    }

    public void setEncoder(Encoder<ILoggingEvent> encoder) {
        this.encoder = encoder;
    }

    private boolean tryToConfigure() {

        if (!CloudWatchLoggingClient.isReady()) {
            return false;
        }

        if (groupName == null) {
            groupName = CloudWatchLoggingClient.getAppName();
        }

        if (streamName == null) {
            streamName = CloudWatchLoggingClient.getHost();
        }

        if (createGroupAndStream) {
            CreateLogGroupRequest createLogGroupRequest = CreateLogGroupRequest.builder().logGroupName(groupName).build();
            try {
                CloudWatchLoggingClient.createLogGroup(createLogGroupRequest);
            } catch (ResourceAlreadyExistsException e) {
                addInfo(String.format("Log group %s already exists", groupName));
            } catch (SdkException e) {
                addError(String.format("Error creating log group %s", groupName), e);
            }

            CreateLogStreamRequest createLogStreamRequest = CreateLogStreamRequest.builder().logStreamName(streamName).logGroupName(groupName).build();
            try {
                CloudWatchLoggingClient.createLogStream(createLogStreamRequest);
            } catch (ResourceAlreadyExistsException e) {
                addInfo(String.format("Log stream %s already exists", streamName));
            } catch (SdkException e) {
                addError(String.format("Error stream log %s", streamName), e);
            }
        }

        configuredSuccessfully = true;

        return true;
    }

    private void dispatchEvents() throws InterruptedException {
        if (!configuredSuccessfully && !tryToConfigure()) {
            return;
        }

        List<InputLogEvent> logEvents = new ArrayList<>(maxBatchSize);
        List<ILoggingEvent> iLoggingEvents = new ArrayList<>(maxBatchSize);

        while (!deque.isEmpty() && logEvents.size() < maxBatchSize) {
            ILoggingEvent event = deque.takeFirst();
            final InputLogEvent inputLogEvent = InputLogEvent.builder().message(
                new String(encoder.encode(event))
            ).timestamp(event.getTimeStamp()).build();

            iLoggingEvents.add(event);
            logEvents.add(inputLogEvent);
        }
        if (!logEvents.isEmpty() && !sendLogsToCloudWatch(logEvents) && emergencyAppender != null) {
            iLoggingEvents.forEach(emergencyAppender::doAppend);
        }

    }

    private boolean sendLogsToCloudWatch(List<InputLogEvent> logEvents) {
        if (sequenceToken == null) {
            try {
                sequenceToken = CloudWatchLoggingClient.getToken(groupName, streamName);
            } catch (SdkException e) {
                addError("Getting token got error", e);
            }
        }
        for (int i = 0; i < PUT_REQUEST_RETRY_COUNT; i++) {
            PutLogEventsRequest putLogEventsRequest = PutLogEventsRequest.builder()
                .logEvents(logEvents)
                .logGroupName(groupName)
                .logStreamName(streamName)
                .sequenceToken(sequenceToken)
                .build();
            try {
                PutLogEventsResponse putLogEventsResponse = CloudWatchLoggingClient.putLogs(putLogEventsRequest);
                if (putLogEventsResponse == null || putLogEventsResponse.nextSequenceToken() == null) {
                    addError("Sending log request failed");
                } else {
                    sequenceToken = putLogEventsResponse.nextSequenceToken();
                    return true;
                }
            } catch (InvalidSequenceTokenException e) {
                sequenceToken = e.expectedSequenceToken();
            } catch (Exception e) {
                addError("Sending log request failed", e);
                return false;
            }
        }
        return false;
    }

    @Override
    public void addAppender(Appender<ILoggingEvent> newAppender) {
        if (emergencyAppender == null) {
            emergencyAppender = newAppender;
        } else {
            addWarn("One and only one appender may be attached to " + getClass().getSimpleName());
            addWarn("Ignoring additional appender named [" + newAppender.getName() + "]");
        }
    }

    @Override
    public Iterator<Appender<ILoggingEvent>> iteratorForAppenders() {
        throw new UnsupportedOperationException("Don't know how to create iterator");
    }

    @Override
    public Appender<ILoggingEvent> getAppender(String name) {
        if (emergencyAppender != null && name != null && name.equals(emergencyAppender.getName())) {
            return emergencyAppender;
        } else {
            return null;
        }
    }

    @Override
    public boolean isAttached(Appender<ILoggingEvent> appender) {
        return (emergencyAppender == appender);
    }

    @Override
    public void detachAndStopAllAppenders() {
        if (emergencyAppender != null) {
            emergencyAppender.stop();
            emergencyAppender = null;
        }
    }

    @Override
    public boolean detachAppender(Appender<ILoggingEvent> appender) {
        if (emergencyAppender == appender) {
            emergencyAppender = null;
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean detachAppender(String name) {
        if (emergencyAppender != null && emergencyAppender.getName().equals(name)) {
            emergencyAppender = null;
            return true;
        } else {
            return false;
        }
    }
}
