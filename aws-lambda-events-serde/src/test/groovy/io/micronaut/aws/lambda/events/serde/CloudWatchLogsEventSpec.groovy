package io.micronaut.aws.lambda.events.serde

import com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent
import io.micronaut.context.BeanContext
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class CloudWatchLogsEventSpec extends Specification {

    @Inject
    ObjectMapper objectMapper

    @Inject
    BeanContext beanContext

    void "test deserialization of cloud watch scheduled event"() {
        given:
        File f = new File('src/test/resources/cloudwatch-logs.json')

        expect:
        f.exists()

        when:
        String json = f.text

        then:
        json

        when:
        CloudWatchLogsEvent event = objectMapper.readValue(json, CloudWatchLogsEvent)

        then:
        event.getAwsLogs()
        "H4sIAAAAAAAAAHWPwQqCQBCGX0Xm7EFtK+smZBEUgXoLCdMhFtKV3akI8d0bLYmibvPPN3wz00CJxmQnTO41whwWQRIctmEcB6sQbFC3CjW3XW8kxpOpP+OC22d1Wml1qZkQGtoMsScxaczKN3plG8zlaHIta5KqWsozoTYw3/djzwhpLwivWFGHGpAFe7DL68JlBUk+l7KSN7tCOEJ4M3/qOI49vMHj+zCKdlFqLaU2ZHV2a4Ct/an0/ivdX8oYc1UVX860fQDQiMdxRQEAAA==" == event.getAwsLogs().data
    }
}
