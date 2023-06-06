package io.micronaut.aws.cloudwatch.logging

import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.core.read.ListAppender
import io.micronaut.context.annotation.Property
import io.micronaut.context.annotation.Replaces
import io.micronaut.context.annotation.Requires
import io.micronaut.context.event.ApplicationEventPublisher
import io.micronaut.runtime.ApplicationConfiguration
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.runtime.server.event.ServerStartupEvent
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import jakarta.inject.Singleton
import ch.qos.logback.classic.Logger
import org.slf4j.LoggerFactory
import software.amazon.awssdk.awscore.exception.AwsServiceException
import software.amazon.awssdk.core.exception.SdkClientException
import software.amazon.awssdk.core.exception.SdkException
import software.amazon.awssdk.services.cloudwatchlogs.CloudWatchLogsClient
import software.amazon.awssdk.services.cloudwatchlogs.model.*
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

@Property(name = "spec.name", value = "CloudwatchLoggingSpec")
@MicronautTest
class CloudwatchLoggingSpec extends Specification {

    @Inject
    CloudWatchLogsClient logging

    @Inject
    ApplicationEventPublisher<ServerStartupEvent> eventPublisher

    @Inject
    ApplicationConfiguration applicationConfiguration

    void "test Cloudwatch logging"() {
        given:
        def logMessage = 'test logging'
        def testHost = 'testHost'
        def logger = LoggerFactory.getLogger(CloudwatchLoggingSpec.class)
        PollingConditions conditions = new PollingConditions(timeout: 10, initialDelay: 1.5, factor: 1.25)
        LoggerContext loggerContext = (LoggerContext) LoggerFactory.getILoggerFactory()
        ListAppender listAppender
        loggerContext.loggerList.each { Logger l ->
            l.iteratorForAppenders().each { appender ->
                if (appender.name == 'AWS') {
                    CloudWatchLoggingAppender cloudWatchLoggingAppender = (CloudWatchLoggingAppender) appender
                    listAppender = (ListAppender) cloudWatchLoggingAppender.getAppender('MOCK')
                }
            }
        }

        when:
        def mockLogging = (MockLogging) logging
        def instance = Mock(EmbeddedServer.class)
        instance.getHost() >> testHost
        def event = new ServerStartupEvent(instance)
        eventPublisher.publishEvent(event)
        logger.info(logMessage)

        then:
        conditions.eventually {
            mockLogging.getPutLogsRequestList().size() != 0
        }

        def putLogRequestList = ((MockLogging) logging).getPutLogsRequestList()
        putLogRequestList.stream().allMatch(x -> x.logGroupName() == applicationConfiguration.getName().get())
        putLogRequestList.stream().allMatch(x -> x.logStreamName() == testHost)

        ObjectMapper mapper = ObjectMapper.getDefault()

        List<Map<String, String>> logEntries = new ArrayList<Map<String, String>>()

        putLogRequestList.forEach(
                x -> {
                    x.logEvents().stream().forEach(y -> logEntries.add(
                            mapper.readValue(y.message(), HashMap.class) as Map<String, String>
                    ))
                }
        )

        logEntries.stream().anyMatch(x -> x.logger == 'io.micronaut.context.DefaultApplicationContext$RuntimeConfiguredEnvironment')
        logEntries.stream().anyMatch(x -> x.logger == 'io.micronaut.aws.cloudwatch.logging.CloudwatchLoggingSpec')
        logEntries.stream().anyMatch(x -> x.message == logMessage)
        listAppender.list.size() == 0

        when:
        mockLogging.state = MockState.NOT_SUCCESSFUL
        logger.info(logMessage)
        conditions = new PollingConditions(timeout: 10, initialDelay: 1.5, factor: 1.25)

        then:
        conditions.eventually {
            listAppender.list.size() == 1
        }
        listAppender.list.get(0).message == logMessage

        when:
        mockLogging.state = MockState.EXCEPTION
        logger.info(logMessage + " from Exception")
        conditions = new PollingConditions(timeout: 10, initialDelay: 1.5, factor: 1.25)

        then:
        conditions.eventually {
            listAppender.list.size() == 2
        }
        listAppender.list.get(1).message == logMessage + " from Exception"
    }

    static enum MockState {
        SUCCESS,
        NOT_SUCCESSFUL,
        EXCEPTION
    }

    @Requires(property = "spec.name", value = "CloudwatchLoggingSpec")
    @Singleton
    @Replaces(CloudWatchLogsClient)
    static class MockLogging implements CloudWatchLogsClient {

        final List<PutLogEventsRequest> putLogsRequestList = Collections.synchronizedList(new ArrayList<>())

        MockState state = MockState.SUCCESS

        Map<String, Integer> numberOfCalls = new HashMap<>()

        @Override
        CreateLogGroupResponse createLogGroup(CreateLogGroupRequest createLogGroupRequest) throws InvalidParameterException, ResourceAlreadyExistsException, LimitExceededException, OperationAbortedException, ServiceUnavailableException, AwsServiceException, SdkClientException, CloudWatchLogsException {
            incrementVisit("createLogGroup")
            if (state == MockState.SUCCESS) {
                return CreateLogGroupResponse.builder().build() as CreateLogGroupResponse
            } else if (state == MockState.NOT_SUCCESSFUL) {
                throw ResourceAlreadyExistsException.builder().build()
            }
            throw SdkException.builder().message("testMessage").build()
        }

        @Override
        CreateLogStreamResponse createLogStream(CreateLogStreamRequest createLogStreamRequest) throws InvalidParameterException, ResourceAlreadyExistsException, ResourceNotFoundException, ServiceUnavailableException, AwsServiceException, SdkClientException, CloudWatchLogsException {
            incrementVisit("createLogStream")
            if (state == MockState.SUCCESS) {
                return CreateLogStreamResponse.builder().build() as CreateLogStreamResponse
            } else if (state == MockState.NOT_SUCCESSFUL) {
                throw ResourceAlreadyExistsException.builder().build()
            }
            throw SdkException.builder().message("testMessage").build()
        }

        @Override
        DescribeLogStreamsResponse describeLogStreams(DescribeLogStreamsRequest describeLogStreamsRequest) throws InvalidParameterException, ResourceNotFoundException, ServiceUnavailableException, AwsServiceException, SdkClientException, CloudWatchLogsException {
            if (state == MockState.SUCCESS) {
                return DescribeLogStreamsResponse.builder()
                        .logStreams(LogStream.builder().uploadSequenceToken("dummyToken")
                                .logStreamName(describeLogStreamsRequest.logStreamNamePrefix()).build())
                        .build() as DescribeLogStreamsResponse
            }
            return DescribeLogStreamsResponse.builder().build() as DescribeLogStreamsResponse
        }

        @Override
        PutLogEventsResponse putLogEvents(PutLogEventsRequest putLogEventsRequest) throws InvalidParameterException, InvalidSequenceTokenException, DataAlreadyAcceptedException, ResourceNotFoundException, ServiceUnavailableException, UnrecognizedClientException, AwsServiceException, SdkClientException, CloudWatchLogsException {
            putLogsRequestList.add(putLogEventsRequest)
            if (state == MockState.SUCCESS) {
                return PutLogEventsResponse.builder().nextSequenceToken("nextSeqToken").build() as PutLogEventsResponse
            } else if (state == MockState.NOT_SUCCESSFUL) {
                return PutLogEventsResponse.builder().build() as PutLogEventsResponse
            } else {
                throw SdkException.builder().message("testMessage").build()
            }
        }

        @Override
        void close() {

        }

        @Override
        String serviceName() {
            return null
        }

        void resetCalls() {
            numberOfCalls = new HashMap<>()
        }

        void incrementVisit(String methodName) {
            numberOfCalls.compute(methodName, (k, v) -> (v == null) ? 1 : v + 1)
        }
    }
}
