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

import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.annotation.JsonInclude
import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.databind.ObjectMapper
import com.amazonaws.services.lambda.runtime.events.CloudFormationCustomResourceEvent
import com.amazonaws.services.lambda.runtime.events.CloudFrontEvent
import com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent
import com.amazonaws.services.lambda.runtime.events.CodeCommitEvent
import com.amazonaws.services.lambda.runtime.events.ConnectEvent
import com.amazonaws.services.lambda.runtime.events.DynamodbEvent
import com.amazonaws.services.lambda.runtime.events.KinesisEvent
import com.amazonaws.services.lambda.runtime.events.SNSEvent
import com.amazonaws.services.lambda.runtime.events.SQSEvent
import com.amazonaws.services.lambda.runtime.events.ScheduledEvent
import com.amazonaws.services.lambda.runtime.events.SecretsManagerRotationEvent
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord
import com.amazonaws.services.lambda.runtime.serialization.events.mixins.CloudFormationCustomResourceEventMixin
import com.amazonaws.services.lambda.runtime.serialization.events.mixins.CloudFrontEventMixin
import com.amazonaws.services.lambda.runtime.serialization.events.mixins.CloudWatchLogsEventMixin
import com.amazonaws.services.lambda.runtime.serialization.events.mixins.CodeCommitEventMixin
import com.amazonaws.services.lambda.runtime.serialization.events.mixins.ConnectEventMixin
import com.amazonaws.services.lambda.runtime.serialization.events.mixins.DynamodbEventMixin
import com.amazonaws.services.lambda.runtime.serialization.events.mixins.KinesisEventMixin
import com.amazonaws.services.lambda.runtime.serialization.events.mixins.SNSEventMixin
import com.amazonaws.services.lambda.runtime.serialization.events.mixins.SQSEventMixin
import com.amazonaws.services.lambda.runtime.serialization.events.mixins.ScheduledEventMixin
import com.amazonaws.services.lambda.runtime.serialization.events.mixins.SecretsManagerRotationEventMixin

/**
 * @author Dan Hollingsworth
 */
class TestJackson {
    private static final ThreadLocal<ObjectMapper> instance = ThreadLocal.withInitial () -> {
        ObjectMapper mapper = new ObjectMapper()
        mapper.setSerializationInclusion JsonInclude.Include.NON_EMPTY
        mapper.addMixIn((Class) CloudFormationCustomResourceEvent, (Class) CloudFormationCustomResourceEventMixin)
        mapper.addMixIn((Class) CloudFrontEvent, (Class) CloudFrontEventMixin)
        mapper.addMixIn((Class) CloudWatchLogsEvent, (Class) CloudWatchLogsEventMixin)
        mapper.addMixIn((Class) CodeCommitEvent, (Class) CodeCommitEventMixin)
        mapper.addMixIn((Class) CodeCommitEvent.Record, (Class) CodeCommitEventMixin.RecordMixin)
        mapper.addMixIn((Class) ConnectEvent, (Class) ConnectEventMixin)
        mapper.addMixIn((Class) ConnectEvent.Details, (Class) ConnectEventMixin.DetailsMixin)
        mapper.addMixIn((Class) ConnectEvent.ContactData, (Class) ConnectEventMixin.ContactDataMixin)
        mapper.addMixIn((Class) ConnectEvent.CustomerEndpoint, (Class) ConnectEventMixin.CustomerEndpointMixin)
        mapper.addMixIn((Class) ConnectEvent.SystemEndpoint, (Class) ConnectEventMixin.SystemEndpointMixin)
        mapper.addMixIn((Class) DynamodbEvent, (Class) DynamodbEventMixin)
        mapper.addMixIn((Class) AttributeValue, (Class) DynamodbEventMixin.AttributeValueMixin)
        mapper.addMixIn((Class) DynamodbEvent.DynamodbStreamRecord, (Class) DynamodbEventMixin.DynamodbStreamRecordMixin)
        mapper.addMixIn((Class) StreamRecord, (Class) DynamodbEventMixin.StreamRecordMixin)
        mapper.addMixIn((Class) KinesisEvent, (Class) KinesisEventMixin)
        mapper.addMixIn((Class) KinesisEvent.Record, (Class) KinesisEventMixin.RecordMixin)
        mapper.addMixIn((Class) ScheduledEvent, (Class) ScheduledEventMixin)
        mapper.addMixIn((Class) SecretsManagerRotationEvent, (Class) SecretsManagerRotationEventMixin)
        mapper.addMixIn((Class) SNSEvent, (Class) SNSEventMixin)
        mapper.addMixIn((Class) SNSEvent.SNSRecord, (Class) SNSEventMixin.SNSRecordMixin)
        mapper.addMixIn((Class) SQSEvent, (Class) SQSEventMixin)
        mapper
    }

    static ObjectMapper get() {
        instance.get()
    }
}
