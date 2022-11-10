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

import io.micronaut.context.annotation.Context;
import io.micronaut.context.event.ApplicationEventListener;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.server.event.ServerStartupEvent;
import jakarta.annotation.PreDestroy;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogGroupRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.CreateLogStreamRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogStream;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsResponse;

import java.util.List;
import java.util.Optional;

/**
 * CloudWatchLoggingClient is a {@link CloudWatchLogsClient} client that is required for {@link CloudWatchLoggingAppender}.
 *
 * @author Nemanja Mikic
 * @since 3.9.0
 */
@Context
@Internal
@Singleton
final class CloudWatchLoggingClient implements ApplicationEventListener<ServerStartupEvent> {

    private static CloudWatchLogsClient logging;
    private static String host;
    private static String appName;
    private final CloudWatchLogsClient internalLogging;
    private final String internalAppName;

    public CloudWatchLoggingClient(CloudWatchLogsClient logging, ApplicationConfiguration applicationConfiguration) {
        this.internalLogging = logging;
        this.internalAppName = applicationConfiguration.getName().orElse("");
    }

    static synchronized boolean isReady() {
        return logging != null;
    }

    static synchronized String getHost() {
        return host;
    }

    static synchronized String getAppName() {
        return appName;
    }

    private static synchronized void setLogging(CloudWatchLogsClient logging, String host, String appName) {
        CloudWatchLoggingClient.logging = logging;
        CloudWatchLoggingClient.host = host;
        CloudWatchLoggingClient.appName = appName;
    }

    static synchronized void destroy() {
        CloudWatchLoggingClient.logging.close();
        CloudWatchLoggingClient.logging = null;
        CloudWatchLoggingClient.host = null;
        CloudWatchLoggingClient.appName = null;
    }

    static synchronized PutLogEventsResponse putLogs(PutLogEventsRequest putLogsRequest) {
        if (logging != null) {
            return logging.putLogEvents(putLogsRequest);
        }
        return null;
    }

    static synchronized void createLogGroup(CreateLogGroupRequest createLogGroupRequest) {
        if (logging != null) {
            logging.createLogGroup(createLogGroupRequest);
        }
    }

    static synchronized void createLogStream(CreateLogStreamRequest createLogStreamRequest) {
        if (logging != null) {
            logging.createLogStream(createLogStreamRequest);
        }
    }

    @Nullable
    static synchronized String getToken(String groupName, String streamName) {
        List<LogStream> logStreams = logging.describeLogStreams(
            DescribeLogStreamsRequest.builder()
                .logGroupName(groupName)
                .logStreamNamePrefix(streamName)
                .build()).logStreams();
        if (!logStreams.isEmpty()) {
            Optional<LogStream> first = logStreams.stream().filter(x -> x.logStreamName().equals(streamName)).findFirst();
            if (first.isPresent()) {
                return first.get().uploadSequenceToken();
            }
        }
        return null;
    }

    @PreDestroy
    public void close() {
        CloudWatchLoggingClient.destroy();
    }

    @Override
    public void onApplicationEvent(ServerStartupEvent event) {
        setLogging(internalLogging, event.getSource().getHost(), internalAppName);
    }

}
