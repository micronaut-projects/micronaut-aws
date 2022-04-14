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

import com.amazonaws.services.lambda.runtime.events.*
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.AttributeValue
import com.amazonaws.services.lambda.runtime.events.models.dynamodb.StreamRecord
import com.amazonaws.services.lambda.runtime.events.models.s3.S3EventNotification
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers
import com.amazonaws.services.lambda.runtime.serialization.events.mixins.*
import com.amazonaws.services.lambda.runtime.serialization.events.modules.DateModule
import com.amazonaws.services.lambda.runtime.serialization.events.modules.DateTimeModule
import io.micronaut.context.BeanContext
import io.micronaut.core.annotation.AnnotationValue
import io.micronaut.core.annotation.Introspected
import io.micronaut.core.beans.BeanIntrospection
import io.micronaut.core.reflect.ReflectionUtils
import io.micronaut.serde.ObjectMapper
import io.micronaut.serde.SerdeIntrospections
import io.micronaut.serde.annotation.SerdeImport
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import org.assertj.core.api.Assertions
import spock.lang.Specification

import java.lang.annotation.Annotation
import java.lang.reflect.Field

@MicronautTest(startApplication = false)
@Introspected(classes = [LambdaEventSerializers])
class AllAwsLambdaEventsSpec extends Specification {

//    - Enhance SerdeImport to have "naming" (and "using", "as", "validate")
//    - Add option to do serialization of byte[] as base64 string instead of byte array
//    - Fix serialization of Map<String, List<String>>
//    - Serialization of long vs date
//    - Serialization of Object causes stack overflow
//    - Instantiate inner classes
//    - S3 events must use reflection-based serialization?

    @Inject
    SerdeIntrospections serde

    @Inject
    ObjectMapper objectMapper

    @Inject
    BeanContext beanContext

    void "Serde imports have the correct mixins"() {
        given:
        Field mixinMap = ReflectionUtils.getRequiredField(LambdaEventSerializers, "MIXIN_MAP");
        mixinMap.setAccessible(true);
        Map<String, Class> eventToMixin = new LinkedHashMap<>(mixinMap.get(null) as Map<String, Class>);

        //not used in AWS lib and not present in classpath
        eventToMixin.remove("com.amazonaws.services.dynamodbv2.model.StreamRecord")
        eventToMixin.remove("com.amazonaws.services.dynamodbv2.model.AttributeValue")

        for (Map.Entry<String, Class> entry : eventToMixin.entrySet()) {
            try {
                when:
                Class<?> eventType = Class.forName(entry.getKey())
                Class<?> expectedEventMixin = entry.getValue()
                BeanIntrospection<?> eventIntrospection =
                        serde.getBeanIntrospector().findIntrospection(eventType)
                                .orElseThrow(() -> new AssertionError("No SerdeImport found for " + entry.getKey(), null))

                /*
                 * TODO
                 *
                 * When both a parent class and one of its inner classes have the same annotation then `getAnnotation`
                 * doesn't reliably (or ever) return the annotation of the parent class. It returns the inner class's
                 * annotation. But isn't the intuitive behavior returning the annotation of the parent class?
                 *
                 * So ideally we could write this code but we can't:
                 * serde.getBeanIntrospector().getIntrospection(eventType).getAnnotation(SerdeImport)
                 *
                 * On the next two lines we'll get annotations from both parent and inner classes and then filter for the parent.
                 */
                List<AnnotationValue<Annotation>> serdeAnnotations = eventIntrospection.getAnnotationValuesByType(SerdeImport)
                AnnotationValue<Annotation> serdeAnnotation =
                        serdeAnnotations.stream()
                                .filter(annotation -> annotation.classValue().get() == eventType)
                                .findAny()
                                .orElseThrow(() -> new AssertionError("Unexpected state", null))

                then:
                Class<?> actualEventMixin = serdeAnnotation.get("mixin", Class)
                        .orElseThrow(() -> new AssertionError(
                                String.format("No SerdeImport mixin found for %s.\nExpects mixin %s",
                                        entry.getKey(), expectedEventMixin.getName()),
                                null))
                Assertions.assertThat(actualEventMixin)
                        .withFailMessage("The SerdeImport %s\nexpects the mixin %s\nbut actually has the mixin %s",
                                entry.getKey(),
                                expectedEventMixin.getName(),
                                actualEventMixin.getName())
                        .isEqualTo(expectedEventMixin)
            } catch (Exception exception) {
                Assertions.fail("Failure encountered when validating the serdeability of " + entry.getKey(), exception);
            }
        }
    }

    void "AWS lambda event can be serialized"(String jsonFilename, Class<Object> type) {
        given:
        File f = new File("src/test/resources/" + jsonFilename)

        expect:
        f.exists()
        f.text

        when:
        String roundTripMicronautJson = objectMapper.writeValueAsString(objectMapper.readValue(f.text, type))
        Map<String, Object> roundTripMicronautMap = objectMapper.readValue(roundTripMicronautJson, Map)

        then:
        TestJackson.get().registerModule(new DateTimeModule(getClass().getClassLoader()))
        TestJackson.get().registerModule(new DateModule())
        TestJackson.get().addMixIn((Class) CloudFormationCustomResourceEvent, (Class) CloudFormationCustomResourceEventMixin)
        TestJackson.get().addMixIn((Class) CloudFrontEvent, (Class) CloudFrontEventMixin)
        TestJackson.get().addMixIn((Class) CloudWatchLogsEvent, (Class) CloudWatchLogsEventMixin)
        TestJackson.get().addMixIn((Class) CodeCommitEvent, (Class) CodeCommitEventMixin)
        TestJackson.get().addMixIn((Class) CodeCommitEvent.Record, (Class) CodeCommitEventMixin.RecordMixin)
        TestJackson.get().addMixIn((Class) ConnectEvent, (Class) ConnectEventMixin)
        TestJackson.get().addMixIn((Class) ConnectEvent.Details, (Class) ConnectEventMixin.DetailsMixin)
        TestJackson.get().addMixIn((Class) ConnectEvent.ContactData, (Class) ConnectEventMixin.ContactDataMixin)
        TestJackson.get().addMixIn((Class) ConnectEvent.CustomerEndpoint, (Class) ConnectEventMixin.CustomerEndpointMixin)
        TestJackson.get().addMixIn((Class) ConnectEvent.SystemEndpoint, (Class) ConnectEventMixin.SystemEndpointMixin)
        TestJackson.get().addMixIn((Class) DynamodbEvent, (Class) DynamodbEventMixin)
        TestJackson.get().addMixIn((Class) DynamodbTimeWindowEvent, (Class) DynamodbEventMixin)
        TestJackson.get().addMixIn((Class) AttributeValue, (Class) DynamodbEventMixin.AttributeValueMixin)
        TestJackson.get().addMixIn((Class) DynamodbEvent.DynamodbStreamRecord, (Class) DynamodbEventMixin.DynamodbStreamRecordMixin)
        TestJackson.get().addMixIn((Class) StreamRecord, (Class) DynamodbEventMixin.StreamRecordMixin)
        TestJackson.get().addMixIn((Class) KinesisEvent, (Class) KinesisEventMixin)
        TestJackson.get().addMixIn((Class) KinesisEvent.Record, (Class) KinesisEventMixin.RecordMixin)
        TestJackson.get().addMixIn((Class) ScheduledEvent, (Class) ScheduledEventMixin)
        TestJackson.get().addMixIn((Class) SecretsManagerRotationEvent, (Class) SecretsManagerRotationEventMixin)
        TestJackson.get().addMixIn((Class) SNSEvent, (Class) SNSEventMixin)
        TestJackson.get().addMixIn((Class) SNSEvent.SNSRecord, (Class) SNSEventMixin.SNSRecordMixin)
        TestJackson.get().addMixIn((Class) SQSEvent, (Class) SQSEventMixin)
        TestJackson.get().addMixIn((Class) SQSEvent.SQSMessage, (Class) SQSEventMixin.SQSMessageMixin)
        String roundTripJacksonJson = TestJackson.get().writeValueAsString(TestJackson.get().readValue(f, type))
        Map<String, Object> roundTripJacksonMap = TestJackson.get().readValue(roundTripJacksonJson, Map)

        roundTripJacksonMap == roundTripMicronautMap

        where:
        jsonFilename                                          | type
        "activemq-event.json"                                 | ActiveMQEvent //Associated with ActiveMQEventSerde
        "apigateway-authorizer.json"                          | APIGatewayCustomAuthorizerEvent //Associated with APIGatewayCustomAuthorizerEventSerde ...
        "api-gateway-proxy.json"                              | APIGatewayProxyRequestEvent
        "apigw-response.json"                                 | APIGatewayProxyResponseEvent
        "apigw-v2-custom-authorizer-v2-request.json"          | APIGatewayV2CustomAuthorizerEvent
        "api-gateway-v2-proxy-request.json"                   | APIGatewayV2ProxyRequestEvent
        "api-gateway-v2-http.json"                            | APIGatewayV2HTTPEvent
        "api-gateway-v2-http-response.json"                   | APIGatewayV2HTTPResponse
        "api-gateway-v2-proxy-response.json"                  | APIGatewayV2ProxyResponseEvent
        "apigw-websocket-request.json"                        | APIGatewayV2WebSocketEvent
        "api-gateway-v2-web-socket-response.json"             | APIGatewayV2WebSocketResponse
        "alb-lambda-target-request-multivalue-headers.json"   | ApplicationLoadBalancerRequestEvent
        "alb-lambda-target-response.json"                     | ApplicationLoadBalancerResponseEvent
        "appsync-lambda-auth-request.json"                    | AppSyncLambdaAuthorizerEvent
        "appsync-lambda-auth-response.json"                   | AppSyncLambdaAuthorizerResponse
        "cloudformation-create-request.json"                  | CloudFormationCustomResourceEvent
        "cloudfront-simple-remote-call.json"                  | CloudFrontEvent
        "cloudwatch-logs.json"                                | CloudWatchLogsEvent
        "codecommit-repository.json"                          | CodeCommitEvent
        "cognito-sync-trigger.json"                           | CognitoEvent
        "cognito-event-userpools-create-auth-challenge.json"  | CognitoUserPoolCreateAuthChallengeEvent
        "cognito-event-userpools-custommessage.json"          | CognitoUserPoolCustomMessageEvent
        "cognito-event-userpools-define-auth-challenge.json"  | CognitoUserPoolDefineAuthChallengeEvent
        "cognito-event.json"                                  | CognitoUserPoolEvent
        "cognito-event-userpools-migrateuser.json"            | CognitoUserPoolMigrateUserEvent
        "cognito-event-userpools-postauthentication.json"     | CognitoUserPoolPostAuthenticationEvent
        "cognito-event-userpools-postconfirmation.json"       | CognitoUserPoolPostConfirmationEvent
        "cognito-event-userpools-preauthentication.json"      | CognitoUserPoolPreAuthenticationEvent
        "cognito-event-userpools-presignup.json"              | CognitoUserPoolPreSignUpEvent
        "cognito-event-userpools-pretokengen.json"            | CognitoUserPoolPreTokenGenerationEvent
        "cognito-event-userpools-verify-auth-challenge.json"  | CognitoUserPoolVerifyAuthChallengeResponseEvent
        "config-event.json"                                   | ConfigEvent
        "connect-event.json"                                  | ConnectEvent
        "dynamodb-update.json"                                | DynamodbEvent
        "dynamodb-time-window.json"                           | DynamodbTimeWindowEvent
        "iam-policy-response.json"                            | IamPolicyResponse
        "iam-policy-response-v1.json"                         | IamPolicyResponseV1
        "iot-button-event.json"                               | IoTButtonEvent
        "kafka-event.json"                                    | KafkaEvent
        "kinesis-analytics-firehose-input-preprocessing.json" | KinesisAnalyticsFirehoseInputPreprocessingEvent
        "kinesis-analytics-input-preprocessing-response.json" | KinesisAnalyticsInputPreprocessingResponse
        "kinesis-analytics-output-delivery-event.json"        | KinesisAnalyticsOutputDeliveryEvent
        "kinesis-analytics-output-delivery-response.json"     | KinesisAnalyticsOutputDeliveryResponse
        "kinesis-analytics-streams-input-preprocessing.json"  | KinesisAnalyticsStreamsInputPreprocessingEvent
        "kinesis-event.json"                                  | KinesisEvent
        "kinesis-firehose-event.json"                         | KinesisFirehoseEvent
        "kinesis-time-window.json"                            | KinesisTimeWindowEvent
        "lambda-destination.json"                             | LambdaDestinationEvent
        "lex-event.json"                                      | LexEvent
        "rabbitmq-event.json"                                 | RabbitMQEvent
        "s3-batch-job-event-request.json"                     | S3BatchEvent
        "s3-batch-job-event-response.json"                    | S3BatchResponse
        "s3-event.json"                                       | S3Event
        "ses-lambda-event.json"                               | S3ObjectLambdaEvent
        "scheduled-event.json"                                | ScheduledEvent
        "secrets-manager-rotation.json"                       | SecretsManagerRotationEvent
        "s3-put.json"                                         | S3EventNotification
        "simple-iam-policy-response.json"                     | SimpleIAMPolicyResponse
        "sns-event.json"                                      | SNSEvent
        "sqs-batch-response.json"                             | SQSBatchResponse
        "sqs-receive-message.json"                            | SQSEvent
        "streams-event-response.json"                         | StreamsEventResponse
        "time-window-event-response.json"                     | TimeWindowEventResponse
    }
}
