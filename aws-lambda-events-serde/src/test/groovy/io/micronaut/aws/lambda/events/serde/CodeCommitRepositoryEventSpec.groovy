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

import com.amazonaws.services.lambda.runtime.events.CodeCommitEvent
import io.micronaut.context.BeanContext
import io.micronaut.serde.ObjectMapper
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.joda.time.DateTime
import org.joda.time.DateTimeZone
import spock.lang.Specification

@MicronautTest(startApplication = false)
class CodeCommitRepositoryEventSpec extends Specification {

    @Inject
    ObjectMapper objectMapper

    @Inject
    BeanContext beanContext

    void "test deserialization of cloud watch scheduled event"() {
        given:
        File f = new File('src/test/resources/codecommit-repository.json')

        expect:
        f.exists()

        when:
        String json = f.text

        then:
        json

        when:
        CodeCommitEvent event = objectMapper.readValue(json, CodeCommitEvent)

        then:
        event.getRecords()
        event.getRecords().size() == 1

        when:
        CodeCommitEvent.Record record = event.getRecords().get(0)

        then:
        "5a824061-17ca-46a9-bbf9-114edeadbeef" == record.eventId
        "1.0" == record.eventVersion
        "my-trigger" == record.eventTriggerName
        1 == record.eventPartNumber
        "TriggerEventTest" == record.eventName
        "5a824061-17ca-46a9-bbf9-114edeadbeef" == record.eventTriggerConfigId
        "arn:aws:codecommit:us-east-1:123456789012:my-repo" == record.eventSourceArn
        "arn:aws:iam::123456789012:root" == record.userIdentityArn
        "aws:codecommit" == record.eventSource
        "us-east-1" == record.awsRegion
        "this is custom data" == record.customData
        1 == record.eventTotalParts
        record.eventTime
        new DateTime(2016, 01, 01, 23, 59, 59, DateTimeZone.forID("UTC")) == record.eventTime
        record.codeCommit
        new CodeCommitEvent.CodeCommit()
                .withReferences([new CodeCommitEvent.Reference()
                                         .withCommit("5c4ef1049f1d27deadbeeff313e0730018be182b")
                                         .withRef("refs/heads/master")]) == record.codeCommit
    }
}
