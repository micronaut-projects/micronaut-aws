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

package io.micronaut.aws.lambda.events.serde;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import io.micronaut.serde.annotation.SerdeImport;

/**
 * {@link SerdeImport} for {@link APIGatewayV2HTTPEvent}.
 *
 * @author Dan Hollingsworth
 */
@SerdeImport(APIGatewayV2HTTPEvent.RequestContext.Authorizer.class)
@SerdeImport(APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT.class)
@SerdeImport(APIGatewayV2HTTPEvent.RequestContext.Http.class)
@SerdeImport(APIGatewayV2HTTPEvent.RequestContext.IAM.class)
@SerdeImport(APIGatewayV2HTTPEvent.RequestContext.CognitoIdentity.class)
@SerdeImport(APIGatewayV2HTTPEvent.RequestContext.class)
@SerdeImport(APIGatewayV2HTTPEvent.class)
public class APIGatewayV2HTTPEventSerde {

}
