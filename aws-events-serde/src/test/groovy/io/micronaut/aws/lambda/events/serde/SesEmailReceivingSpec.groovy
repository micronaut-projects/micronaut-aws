package io.micronaut.aws.lambda.events.serde

import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification
import io.micronaut.context.BeanContext
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.PendingFeature
import spock.lang.Specification

@MicronautTest(startApplication = false)
class S3PutJsonSpec extends Specification {
    @Inject
    ObjectMapper objectMapper

    @Inject
    BeanContext beanContext

    @PendingFeature
    void "S3EventNotification can be serialized"() {
        given:
        File f = new File("src/test/resources/s3-put.json")

        expect:
        f.exists()

        when:
        String json = f.text
        S3EventNotification event = objectMapper.readValue(json, S3EventNotification)

        then:
        event
        event.records
    }

}