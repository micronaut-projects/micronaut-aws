package io.micronaut.aws.lambda.events.serde

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import io.micronaut.json.JsonMapper
import spock.lang.Specification

class DynamodbEventSpec extends Specification {

    JsonMapper objectMapper = CustomPojoSerializerUtils.getJsonMapper()

    void "test deserialization of sqs recieve message event"() {
        given:
        File f = new File('src/test/resources/dynamodb-update.json')

        expect:
        f.exists()

        when:
        String json = f.text

        then:
        json

        when:
        DynamodbEvent sqsEvent = objectMapper.readValue(json, DynamodbEvent)

        then:
        sqsEvent.getRecords().size() == 3
    }
}
