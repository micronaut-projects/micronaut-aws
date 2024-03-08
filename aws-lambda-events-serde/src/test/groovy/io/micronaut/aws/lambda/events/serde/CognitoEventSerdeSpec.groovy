package io.micronaut.aws.lambda.events.serde

import com.amazonaws.services.lambda.runtime.events.CognitoEvent
import io.micronaut.json.JsonMapper
import spock.lang.Specification

class CognitoEventSerdeSpec extends Specification {

    JsonMapper objectMapper = CustomPojoSerializerUtils.getJsonMapper()

    void "test deserialization of cloud watch scheduled event"() {
        given:
        File f = new File('src/test/resources/cognito-sync-trigger.json')

        expect:
        f.exists()

        when:
        String json = f.text

        then:
        json

        when:
        CognitoEvent event = objectMapper.readValue(json, CognitoEvent)

        then:
        event
        2 == event.version
        "SyncTrigger" == event.eventType
        "us-east-1" == event.region
        "identityPoolId" == event.identityPoolId
        "identityId" == event.identityId
        "datasetName" == event.datasetName
        new CognitoEvent.DatasetRecord()
                .withOldValue("oldValue1")
                .withNewValue("newValue1")
                .withOp("replace") == event.getDatasetRecords().get("SampleKey1")
        new CognitoEvent.DatasetRecord()
                .withOldValue("oldValue2")
                .withNewValue("newValue2")
                .withOp("replace") == event.getDatasetRecords().get("SampleKey2")
    }
}
