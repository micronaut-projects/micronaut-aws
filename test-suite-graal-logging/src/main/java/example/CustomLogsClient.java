package example;

import io.micronaut.context.annotation.Replaces;
import jakarta.inject.Singleton;
import software.amazon.awssdk.awscore.exception.AwsServiceException;
import software.amazon.awssdk.core.exception.SdkClientException;
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.DescribeLogStreamsResponse;
import software.amazon.awssdk.services.cloudwatchlogs.model.InputLogEvent;
import software.amazon.awssdk.services.cloudwatchlogs.model.LogStream;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsRequest;
import software.amazon.awssdk.services.cloudwatchlogs.model.PutLogEventsResponse;

import java.util.ArrayList;
import java.util.List;

@Singleton
@Replaces(CloudWatchLogsClient.class)
public class CustomLogsClient implements CloudWatchLogsClient {

    private final List<String> loggedMessages = new ArrayList<>();

    @Override
    public String serviceName() {
        return null;
    }

    @Override
    public void close() {

    }

    @Override
    public DescribeLogStreamsResponse describeLogStreams(DescribeLogStreamsRequest describeLogStreamsRequest)
        throws AwsServiceException, SdkClientException {
        LogStream logStream = LogStream.builder()
            .logStreamName("testStreamName")
            .uploadSequenceToken("testSequenceToken")
            .build();
        return DescribeLogStreamsResponse.builder()
            .logStreams(logStream)
            .build();
    }

    @Override
    public PutLogEventsResponse putLogEvents(PutLogEventsRequest putLogEventsRequest)
        throws AwsServiceException, SdkClientException {
        for (InputLogEvent inputLogEvent : putLogEventsRequest.logEvents()) {
            loggedMessages.add(inputLogEvent.message());
        }
        return PutLogEventsResponse.builder()
            .nextSequenceToken("testNextSequenceToken")
            .build();
    }

    public List<String> getLoggedMessages() {
        return loggedMessages;
    }
}
