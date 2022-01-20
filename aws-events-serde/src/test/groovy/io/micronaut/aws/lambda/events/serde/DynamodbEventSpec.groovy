package io.micronaut.aws.lambda.events.serde

import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import io.micronaut.context.BeanContext
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification


@MicronautTest(startApplication = false)
class DynamodbEventSpec extends Specification {

    @Inject
    ObjectMapper objectMapper

    @Inject
    BeanContext beanContext

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
