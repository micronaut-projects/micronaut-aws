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
import com.amazonaws.services.lambda.runtime.serialization.events.LambdaEventSerializers
import com.amazonaws.services.lambda.runtime.serialization.events.mixins.*
import com.amazonaws.services.lambda.runtime.serialization.events.modules.DateModule
import com.amazonaws.services.lambda.runtime.serialization.events.modules.DateTimeModule
import io.micronaut.context.BeanContext
import io.micronaut.core.annotation.Introspected
import io.micronaut.core.reflect.ReflectionUtils
import io.micronaut.serde.ObjectMapper
import io.micronaut.serde.SerdeIntrospections
import io.micronaut.serde.annotation.SerdeImport
import io.micronaut.test.extensions.spock.annotation.MicronautTest
import jakarta.inject.Inject
import spock.lang.Specification

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

        when:
        Map<Class<?>, Class<?>> importedMixins = importedMixins()
        List<String> problems = []
        for (Map.Entry<String, Class> entry : eventToMixin.entrySet()) {
            String problem = mixinProblem(entry.getKey(), entry.getValue(), importedMixins)
            if (problem != null) {
                problems << problem
            }
        }

        then:
        !importedMixins.isEmpty()
        problems.empty
    }

    /**
     * The mixin each {@link SerdeImport} of this module declares, by imported type. Read from the import classes
     * themselves: the serde processor does not copy {@code @SerdeImport} onto the introspection it generates.
     */
    private static Map<Class<?>, Class<?>> importedMixins() {
        String pkg = AllAwsLambdaEventsSpec.package.name
        Map<Class<?>, Class<?>> mixins = [:]
        // the package spans the main and the test output directories
        Collections.list(AllAwsLambdaEventsSpec.classLoader.getResources(pkg.replace('.', '/')))
                .findAll { it.protocol == 'file' }
                .collectMany { new File(it.toURI()).listFiles().toList() }
                .findAll { it.name.endsWith('Serde.class') }
                .collect { Class.forName(pkg + '.' + it.name - '.class') }
                .each { Class<?> importClass ->
                    for (SerdeImport serdeImport : importClass.getAnnotationsByType(SerdeImport)) {
                        mixins.put(serdeImport.value(), serdeImport.mixin())
                    }
                }
        return mixins
    }

    /**
     * @return why the SerdeImport of the event does not declare the mixin the AWS library uses, or null if it does
     */
    private static String mixinProblem(String eventName, Class<?> expectedEventMixin, Map<Class<?>, Class<?>> importedMixins) {
        Class<?> eventType
        try {
            eventType = Class.forName(eventName)
        } catch (ClassNotFoundException e) {
            return "${eventName}: not on the classpath"
        }
        if (!importedMixins.containsKey(eventType)) {
            return "${eventName}: no SerdeImport (expects mixin ${expectedEventMixin.name})"
        }
        Class<?> actualEventMixin = importedMixins.get(eventType)
        if (actualEventMixin != expectedEventMixin) {
            return "${eventName}: expects mixin ${expectedEventMixin.name} but has ${actualEventMixin.name}"
        }
        return null
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
        "api-gateway-v2-http.json"                            | APIGatewayV2HTTPEvent
        "api-gateway-v2-http-response.json"                   | APIGatewayV2HTTPResponse
        "api-gateway-v2-proxy-response.json"                  | APIGatewayV2ProxyResponseEvent
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
        "cognito-event-userpools-migrateuser.json"            | CognitoUserPoolMigrateUserEvent
        "cognito-event-userpools-postauthentication.json"     | CognitoUserPoolPostAuthenticationEvent
        "cognito-event-userpools-postconfirmation.json"       | CognitoUserPoolPostConfirmationEvent
        "cognito-event-userpools-preauthentication.json"      | CognitoUserPoolPreAuthenticationEvent
        "cognito-event-userpools-presignup.json"              | CognitoUserPoolPreSignUpEvent
        "cognito-event-userpools-pretokengen.json"            | CognitoUserPoolPreTokenGenerationEvent
        "cognito-event-userpools-verify-auth-challenge.json"  | CognitoUserPoolVerifyAuthChallengeResponseEvent
        "config-event.json"                                   | ConfigEvent
        //"connect-event.json"                                  | ConnectEvent
        "iot-button-event.json"                               | IoTButtonEvent
        "kinesis-analytics-input-preprocessing-response.json" | KinesisAnalyticsInputPreprocessingResponse
        "kinesis-analytics-output-delivery-response.json"     | KinesisAnalyticsOutputDeliveryResponse
        "lambda-destination.json"                             | LambdaDestinationEvent
        "rabbitmq-event.json"                                 | RabbitMQEvent
        "s3-batch-job-event-request.json"                     | S3BatchEvent
        "s3-batch-job-event-response.json"                    | S3BatchResponse
        "scheduled-event.json"                                | ScheduledEvent
        "secrets-manager-rotation.json"                       | SecretsManagerRotationEvent
        "simple-iam-policy-response.json"                     | SimpleIAMPolicyResponse
        "sqs-batch-response.json"                             | SQSBatchResponse
        "sqs-receive-message.json"                            | SQSEvent
        "streams-event-response.json"                         | StreamsEventResponse
        "time-window-event-response.json"                     | TimeWindowEventResponse

//        and:
//        "kafka-event.json"                                    | KafkaEvent
//        "sns-event.json"                                      | SNSEvent
//        "s3-put.json"                                         | S3EventNotification
//        "sns-event.json"                                      | SNSEvent
//        "ses-lambda-event.json"                               | S3ObjectLambdaEvent
//        "s3-event.json"                                       | S3Event
//        "lex-event.json"                                      | LexEvent
//        "kinesis-analytics-firehose-input-preprocessing.json" | KinesisAnalyticsFirehoseInputPreprocessingEvent
//        "kinesis-analytics-output-delivery-event.json"        | KinesisAnalyticsOutputDeliveryEvent
//        "kinesis-analytics-streams-input-preprocessing.json"  | KinesisAnalyticsStreamsInputPreprocessingEvent
//        "kinesis-event.json"                                  | KinesisEvent
//        "kinesis-firehose-event.json"                         | KinesisFirehoseEvent
//        "kinesis-time-window.json"                            | KinesisTimeWindowEvent
//        "apigw-v2-custom-authorizer-v2-request.json"          | APIGatewayV2CustomAuthorizerEvent
//        "api-gateway-v2-proxy-request.json"                   | APIGatewayV2ProxyRequestEvent
//        "apigw-websocket-request.json"                        | APIGatewayV2WebSocketEvent
//        "cognito-event.json"                                  | CognitoUserPoolEvent
//        "dynamodb-update.json"                                | DynamodbEvent
//        "dynamodb-time-window.json"                           | DynamodbTimeWindowEvent
//        "iam-policy-response.json"                            | IamPolicyResponse
//        "iam-policy-response-v1.json"                         | IamPolicyResponseV1
    }
}
