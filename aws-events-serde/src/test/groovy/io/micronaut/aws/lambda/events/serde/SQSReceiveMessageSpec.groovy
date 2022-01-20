package io.micronaut.aws.lambda.events.serde

import com.amazonaws.services.lambda.runtime.events.SQSEvent
import io.micronaut.context.BeanContext
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.PendingFeature
import spock.lang.Specification

@MicronautTest(startApplication = false)
class SQSReceiveMessageSpec extends Specification {

    @Inject
    ObjectMapper objectMapper

    @Inject
    BeanContext beanContext

    void "test deserialization of sqs recieve message event"() {
        given:
        File f = new File('src/test/resources/sqs-receive-message.json')

        expect:
        f.exists()

        when:
        String json = f.text

        then:
        json

        when:
        SQSEvent sqsEvent = objectMapper.readValue(json, SQSEvent)

        then:
        sqsEvent.getRecords().size() == 1

        when:
        SQSEvent.SQSMessage sqsMessage = sqsEvent.getRecords().get(0)

        then:
        "19dd0b57-b21e-4ac1-bd88-01bbb068cb78" == sqsMessage.getMessageId()
        "MessageReceiptHandle" == sqsMessage.getReceiptHandle()
        "Hello from SQS!" == sqsMessage.getBody()
        [:] == sqsMessage.getMessageAttributes()
        "{{{md5_of_body}}}" == sqsMessage.getMd5OfBody()
        "aws:sqs" == sqsMessage.getEventSource()
        "arn:aws:sqs:us-east-1:123456789012:MyQueue" == sqsMessage.getEventSourceArn()
        "us-east-1" == sqsMessage.getAwsRegion()
        sqsMessage.getAttributes()
        "1" == sqsMessage.getAttributes().get("ApproximateReceiveCount")
        "1523232000000" == sqsMessage.getAttributes().get("SentTimestamp")
        "123456789012" == sqsMessage.getAttributes().get("SenderId")
        "1523232000001" == sqsMessage.getAttributes().get("ApproximateFirstReceiveTimestamp")
    }
}
