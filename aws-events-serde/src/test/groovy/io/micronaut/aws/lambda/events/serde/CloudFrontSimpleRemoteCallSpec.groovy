package io.micronaut.aws.lambda.events.serde

import com.amazonaws.services.lambda.runtime.events.CloudFrontEvent
import io.micronaut.context.BeanContext
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

@MicronautTest(startApplication = false)
class CloudFrontSimpleRemoteCallSpec extends Specification {

    @Inject
    ObjectMapper objectMapper

    @Inject
    BeanContext beanContext

    void "test deserialization of cloudfront simple remote call event"() {
        given:
        File f = new File('src/test/resources/cloudfront-simple-remote-call.json')

        expect:
        f.exists()

        when:
        String json = f.text

        then:
        json

        when:
        CloudFrontEvent event = objectMapper.readValue(json, CloudFrontEvent)

        then:
        event.getRecords().size() == 1

        when:
        CloudFrontEvent.Record record = event.getRecords().get(0)

        then:
        record.getCf()
        "EXAMPLE" == record.getCf().getConfig().getDistributionId()
        "/test" == record.getCf().getRequest().getUri()
        "GET" == record.getCf().getRequest().getMethod()
        "2001:cdba::3257:9652" == record.getCf().getRequest().getClientIp()
        [
            "host": [new CloudFrontEvent.Header().withKey("Host").withValue("d123.cf.net")],
            "user-agent": [new CloudFrontEvent.Header().withKey("User-Agent").withValue("Test Agent")],
            "user-name": [new CloudFrontEvent.Header().withKey("User-Name").withValue("aws-cloudfront")],
        ] == record.getCf().getRequest().getHeaders()
    }
}
