/*
 * Copyright 2017-2023 original authors
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
package io.micronaut.aws.lambda.events.serde;

import io.micronaut.core.annotation.TypeHint;
import static io.micronaut.core.annotation.TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS;
import static io.micronaut.core.annotation.TypeHint.AccessType.ALL_DECLARED_FIELDS;
import static io.micronaut.core.annotation.TypeHint.AccessType.ALL_DECLARED_METHODS;
import static io.micronaut.core.annotation.TypeHint.AccessType.ALL_PUBLIC;
import static io.micronaut.core.annotation.TypeHint.AccessType.ALL_PUBLIC_CONSTRUCTORS;
import static io.micronaut.core.annotation.TypeHint.AccessType.ALL_PUBLIC_FIELDS;
import static io.micronaut.core.annotation.TypeHint.AccessType.ALL_PUBLIC_METHODS;

/**
 * Type Hint annotations for ease GraalVM integration for AWS Lambda Java Events.
 * @author Sergio del Amo
 * @since 4.0.0
 */
@TypeHint(
    accessType = {
        ALL_PUBLIC,
        ALL_DECLARED_CONSTRUCTORS,
        ALL_PUBLIC_CONSTRUCTORS,
        ALL_DECLARED_METHODS,
        ALL_DECLARED_FIELDS,
        ALL_PUBLIC_METHODS, ALL_PUBLIC_FIELDS
    },
    value = {
        com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerRequestEvent.class,
        com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerResponseEvent.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.ProxyRequestContext.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent.RequestIdentity.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent.class,
        com.amazonaws.services.lambda.runtime.events.ScheduledEvent.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent.class,
        com.amazonaws.services.lambda.runtime.events.APIGatewayV2ProxyResponseEvent.class,
        com.amazonaws.services.lambda.runtime.events.CloudFrontEvent.class,
        com.amazonaws.services.lambda.runtime.events.CloudWatchLogsEvent.class,
        com.amazonaws.services.lambda.runtime.events.CodeCommitEvent.class,
        com.amazonaws.services.lambda.runtime.events.CognitoEvent.class,
        com.amazonaws.services.lambda.runtime.events.ConfigEvent.class,
        com.amazonaws.services.lambda.runtime.events.IoTButtonEvent.class,
        com.amazonaws.services.lambda.runtime.events.LexEvent.class,
        com.amazonaws.services.lambda.runtime.events.SNSEvent.class,
        com.amazonaws.services.lambda.runtime.events.SQSEvent.class
    }
)
public final class AwsLambdaEventsTypeHint {
    private AwsLambdaEventsTypeHint() {
    }
}
