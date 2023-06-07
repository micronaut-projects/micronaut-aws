/*
 * Copyright 2022 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

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
                "host"      : [new CloudFrontEvent.Header().withKey("Host").withValue("d123.cf.net")],
                "user-agent": [new CloudFrontEvent.Header().withKey("User-Agent").withValue("Test Agent")],
                "user-name" : [new CloudFrontEvent.Header().withKey("User-Name").withValue("aws-cloudfront")],
        ] == record.getCf().getRequest().getHeaders()
    }
}
