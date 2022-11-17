package io.micronaut.aws.cloudwatch.logging

import ch.qos.logback.classic.Level
import ch.qos.logback.classic.LoggerContext
import ch.qos.logback.classic.PatternLayout
import ch.qos.logback.classic.spi.LoggingEvent
import ch.qos.logback.core.encoder.LayoutWrappingEncoder
import io.micronaut.runtime.ApplicationConfiguration
import io.micronaut.runtime.server.EmbeddedServer
import io.micronaut.runtime.server.event.ServerStartupEvent
import spock.lang.Specification
import spock.util.concurrent.PollingConditions

class CloudWatchLoggingAppenderSpec extends Specification {

    CloudWatchLoggingAppender appender
    LoggerContext context
    PatternLayout layout
    LayoutWrappingEncoder encoder
    CloudwatchLoggingSpec.MockLogging cloudWatchLogsClient

    def setup() {
        context = new LoggerContext()
        layout = new PatternLayout()
        layout.context = context
        layout.pattern = "[%thread] %level %logger{20} - %msg%n%xThrowable"
        layout.start()
        encoder = new LayoutWrappingEncoder()
        encoder.layout = layout
        encoder.start()
        appender = new CloudWatchLoggingAppender()
        appender.context = context
        appender.encoder = encoder
        def config = Stub(ApplicationConfiguration) {
            getName() >> Optional.of("my-awesome-app")
        }
        def instance = Mock(EmbeddedServer.class)
        instance.getHost() >> "testHost"
        def serverStartupEvent = new ServerStartupEvent(instance)

        cloudWatchLogsClient = new CloudwatchLoggingSpec.MockLogging()

        new CloudWatchLoggingClient(cloudWatchLogsClient, config).onApplicationEvent(serverStartupEvent)

    }

    def cleanup() {
        layout.stop()
        encoder.stop()
        appender.stop()
        CloudWatchLoggingClient.destroy()
    }

    void 'test error queue size less then 0'() {
        when:
        appender.queueSize = -1
        appender.start()

        then:
        def statuses = context.getStatusManager().getCopyOfStatusList()
        statuses.find { it.message == "Queue size must be greater than zero" }
    }

    void 'test error queue size equal to 0'() {
        when:
        appender.queueSize = 0
        appender.start()

        then:
        def statuses = context.getStatusManager().getCopyOfStatusList()
        statuses.find { it.message == "Queue size of zero is deprecated, use a size of one to indicate synchronous processing" }
    }

    void 'test error publish period less or equal to 0'() {
        when:
        appender.queueSize = 100
        appender.publishPeriod = 0
        appender.start()

        then:
        def statuses = context.getStatusManager().getCopyOfStatusList()
        statuses.find { it.message == "Publish period must be greater than zero" }
    }

    void 'test error max batch size less or equal to 0'() {
        when:
        appender.maxBatchSize = 0
        appender.start()

        then:
        def statuses = context.getStatusManager().getCopyOfStatusList()
        statuses.find { it.message == "Max Batch size must be greater than zero" }
    }

    void 'encoder not set'() {
        when:
        appender.queueSize = 100
        appender.publishPeriod = 100
        appender.encoder = null
        appender.start()

        then:
        def statuses = context.getStatusManager().getCopyOfStatusList()
        statuses.find { it.message == "No encoder set for the appender named [null]." }
    }

    void 'register multiple emergency appender'() {
        when:
        def mockAppender = new MockAppender()
        appender.queueSize = 100
        appender.publishPeriod = 100
        appender.encoder = new LayoutWrappingEncoder()
        appender.addAppender(mockAppender)
        appender.addAppender(mockAppender)

        then:
        def statuses = context.getStatusManager().getCopyOfStatusList()
        statuses.find { it.message == "One and only one appender may be attached to CloudWatchLoggingAppender" }
        statuses.find { it.message == "Ignoring additional appender named [MockAppender]" }
        appender.getAppender("MockAppender") != null
        appender.getAppender("NotExistingOne") == null
        appender.isAttached(mockAppender)
        appender.encoder != null
        appender.queueSize == 100
        appender.publishPeriod == 100

        appender.detachAndStopAllAppenders()
        !appender.isAttached(mockAppender)
    }

    void 'detach emergency appender by name'() {
        when:
        def mockAppender = new MockAppender()
        appender.queueSize = 100
        appender.publishPeriod = 100
        appender.encoder = new LayoutWrappingEncoder()
        appender.addAppender(mockAppender)

        then:
        appender.detachAppender("MockAppender")
        !appender.detachAppender("NotExistingOne")
    }

    void 'detach emergency appender by instance'() {
        when:
        def mockAppender = new MockAppender()
        appender.queueSize = 100
        appender.publishPeriod = 100
        appender.encoder = new LayoutWrappingEncoder()
        appender.addAppender(mockAppender)

        then:
        appender.detachAppender(mockAppender)
        !appender.detachAppender(mockAppender)
    }

    void 'try to create iterator for emergency appender'() {
        when:
        def mockAppender = new MockAppender()
        appender.queueSize = 100
        appender.publishPeriod = 100
        appender.encoder = new LayoutWrappingEncoder()
        appender.addAppender(mockAppender)
        appender.iteratorForAppenders()

        then:
        thrown(UnsupportedOperationException)
    }

    void 'custom groupName and StreamName'() {
        given:
        def testGroup = "testGroup"
        def testStream = "testStream"
        def testMessage = "testMessage"
        PollingConditions conditions = new PollingConditions(timeout: 10, initialDelay: 1.5, factor: 1.25)
        LoggingEvent event = createEvent("name", Level.INFO, testMessage, System.currentTimeMillis())

        when:
        appender.groupName = testGroup
        appender.streamName = testStream
        appender.start()
        appender.doAppend(event)

        then:
        appender.groupName == testGroup
        appender.streamName == testStream
        conditions.eventually {
            cloudWatchLogsClient.putLogsRequestList.size() == 1
        }
        cloudWatchLogsClient.putLogsRequestList.get(0).logGroupName() == testGroup
        cloudWatchLogsClient.putLogsRequestList.get(0).logStreamName() == testStream

    }

    void 'test create group and stream flag'() {
        given:
        def testMessage = "testMessage"
        PollingConditions conditions = new PollingConditions(timeout: 10, initialDelay: 1.5, factor: 1.25)
        LoggingEvent event = createEvent("name", Level.INFO, testMessage, System.currentTimeMillis())

        when:
        appender.maxBatchSize = 10
        appender.createGroupAndStream = false
        appender.groupName = "test"
        appender.streamName = "test"
        cloudWatchLogsClient.resetCalls()
        appender.start()
        appender.doAppend(event)

        then:
        appender.maxBatchSize == 10
        !appender.isCreateGroupAndStream()
        conditions.eventually {
            cloudWatchLogsClient.putLogsRequestList.size() == 1
        }
        cloudWatchLogsClient.numberOfCalls.isEmpty()
    }

    void 'test resource already exists when creating group and stream'() {
        given:
        def testMessage = "testMessage2"
        def testGroup = "testGroup"
        def testStream = "testStream"
        PollingConditions conditions = new PollingConditions(timeout: 10, initialDelay: 1.5, factor: 1.25)
        LoggingEvent event = createEvent("name", Level.INFO, testMessage, System.currentTimeMillis())

        when:
        appender.groupName = testGroup
        appender.streamName = testStream
        cloudWatchLogsClient.state = CloudwatchLoggingSpec.MockState.NOT_SUCCESSFUL
        appender.start()
        appender.doAppend(event)

        then:
        conditions.eventually {
            cloudWatchLogsClient.putLogsRequestList.size() == 2
        }
        def statuses = context.getStatusManager().getCopyOfStatusList()
        statuses.find { it.message == "Log group " + testGroup + " already exists" }
        statuses.find { it.message == "Log stream " + testStream + " already exists" }
        statuses.find { it.message == "Sending log request failed" }
    }

    void 'test exception handling'() {
        given:
        def testMessage = "testMessage2"
        def testGroup = "testGroup"
        def testStream = "testStream"

        PollingConditions conditions = new PollingConditions(timeout: 10, initialDelay: 1.5, factor: 1.25)
        LoggingEvent event = createEvent("name", Level.INFO, testMessage, System.currentTimeMillis())

        when:
        appender.groupName = testGroup
        appender.streamName = testStream
        cloudWatchLogsClient.state = CloudwatchLoggingSpec.MockState.EXCEPTION
        appender.start()
        appender.doAppend(event)

        then:
        conditions.eventually {
            cloudWatchLogsClient.putLogsRequestList.size() == 1
        }
        def statuses = context.getStatusManager().getCopyOfStatusList()
        statuses.find { it.message == "Error creating log group " + testGroup }
        statuses.find { it.message == "Error stream log " + testStream }
        statuses.find { it.message == "Sending log request failed" }
        statuses.findAll { it.throwable != null }.size() == 3
    }

    LoggingEvent createEvent(String name, Level level, String message, Long time) {
        LoggingEvent event = new LoggingEvent()
        event.loggerName = name
        event.level = level
        event.message = message
        if (time != null) {
            event.timeStamp = time
        }
        return event
    }

}
