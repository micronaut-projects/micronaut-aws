package io.micronaut.aws.lambda.events.serde

import com.amazonaws.services.lambda.runtime.events.S3Event
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification
import io.micronaut.context.BeanContext
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.PendingFeature
import spock.lang.Specification

@MicronautTest(startApplication = false)
class S3EventNotificationSpec extends Specification {
    @Inject
    ObjectMapper objectMapper

    @Inject
    BeanContext beanContext

    void "S3EventNotification can be serialized with s3-put"() {
        given:
        File f = new File("src/test/resources/s3-put.json")

        expect:
        f.exists()

        when:
        String json = f.text
        S3EventNotification event = objectMapper.readValue(json, S3EventNotification)

        then:
        assertionsS3Put(event)

        when:
        json = objectMapper.writeValueAsString(event)

        then:
        json.contains("\"x-amz-id-2\":\"EXAMPLE123/5678abcdefghijklambdaisawesome/mnopqrstuvwxyzABCDEFGH\"")
        json.contains("\"x-amz-request-id\":\"EXAMPLE123456789\"")
    }

    void "S3EventNotification can be serialized with s3-event"() {
        given:
        File f = new File("src/test/resources/s3-event.json")

        expect:
        f.exists()

        when:
        String json = f.text
        S3EventNotification event = objectMapper.readValue(json, S3EventNotification)

        then:
        assertionsS3Event(event)
    }

    @PendingFeature
    void "S3Event can be deserialized with s3-put"() {
        given:
        File f = new File("src/test/resources/s3-put.json")

        expect:
        f.exists()

        when:
        String json = f.text
        S3Event event = objectMapper.readValue(json, S3Event)

        then:
        assertionsS3Put(event)
    }

    @PendingFeature
    void "S3Event can be serialized with s3-event"() {
        given:
        File f = new File("src/test/resources/s3-event.json")

        expect:
        f.exists()

        when:
        String json = f.text
        S3Event event = objectMapper.readValue(json, S3Event)

        then:
        assertionsS3Event(event)
    }


    void assertionsS3Event(S3EventNotification event) {
        assert event
        assert event.records
        assert "2.0" == event.records[0].eventVersion
        assert "aws:s3" == event.records[0].eventSource
        assert "us-east-1" == event.records[0].awsRegion
        assert "1970-01-01T00:00:00.123Z" == event.records[0].eventTime.toString()
        assert "ObjectCreated:Put" == event.records[0].eventName
        assert "EXAMPLE" == event.records[0].userIdentity.principalId
        assert "127.0.0.1" == event.records[0].requestParameters.sourceIPAddress
        assert "FMyUVURIY8/IgAtTv8xRjskZQpcIZ9KG4V5Wp6S7S/JRWeUWerMUE5JgHvANOjpD" == event.records[0].responseElements.xAmzId2
        assert "C3D13FE58DE4C810" == event.records[0].responseElements.xAmzRequestId
        assert "1.0" == event.records[0].s3.s3SchemaVersion
        assert "testConfigRule" == event.records[0].s3.configurationId
        assert "sourcebucket" == event.records[0].s3.bucket.name
        assert "EXAMPLE" == event.records[0].s3.bucket.ownerIdentity.principalId
        assert "arn:aws:s3:::mybucket" == event.records[0].s3.bucket.arn
        assert "Happy%20Face.jpg" == event.records[0].s3.object.key
        assert 1024 == event.records[0].s3.object.size
        assert "version" == event.records[0].s3.object.versionId
        assert "d41d8cd98f00b204e9800998ecf8427e" == event.records[0].s3.object.eTag
        assert "Happy Sequencer" == event.records[0].s3.object.sequencer
    }

    void assertionsS3Put(S3EventNotification event) {
        assert event
        assert event.records != null
        assert event.records.size() > 0
        assert "2.0" == event.records[0].eventVersion
        assert "aws:s3" == event.records[0].eventSource
        assert "us-east-1" == event.records[0].awsRegion
        assert "1970-01-01T00:00:00.000Z" == event.records[0].eventTime.toString()
        assert "ObjectCreated:Put" == event.records[0].eventName
        assert "EXAMPLE" == event.records[0].userIdentity.principalId
        assert "127.0.0.1" == event.records[0].requestParameters.sourceIPAddress
        assert "EXAMPLE123456789" == event.records[0].responseElements.xAmzRequestId
        assert "EXAMPLE123/5678abcdefghijklambdaisawesome/mnopqrstuvwxyzABCDEFGH" == event.records[0].responseElements.xAmzId2
        assert "1.0" == event.records[0].s3.s3SchemaVersion
        assert "testConfigRule" == event.records[0].s3.configurationId
        assert "example-bucket" == event.records[0].s3.bucket.name
        assert "EXAMPLE" == event.records[0].s3.bucket.ownerIdentity.principalId
        assert "arn:aws:s3:::example-bucket" == event.records[0].s3.bucket.arn
        assert "test%2Fkey" == event.records[0].s3.object.key
        assert 1024 == event.records[0].s3.object.size
        assert "0123456789abcdef0123456789abcdef" == event.records[0].s3.object.eTag
        assert "0A1B2C3D4E5F678901" == event.records[0].s3.object.sequencer
    }

}
