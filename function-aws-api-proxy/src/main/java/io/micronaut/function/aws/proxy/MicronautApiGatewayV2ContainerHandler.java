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
package io.micronaut.function.aws.proxy;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.function.aws.proxy.transformer.httpgw.MicronautApiGatewayV2RequestTransformer;
import io.micronaut.function.aws.proxy.transformer.httpgw.MicronautApiGatewayV2ResponseTransformer;
import io.micronaut.serde.annotation.SerdeImport;
import jakarta.inject.Inject;

@TypeHint(
    accessType = {
        TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS,
        TypeHint.AccessType.ALL_PUBLIC
    },
    value = MicronautApiGatewayV2ContainerHandler.class
)
@Introspected(classes = {
    MicronautApiGatewayV2ContainerHandler.class,
    APIGatewayV2HTTPEvent.class,
    APIGatewayV2HTTPEvent.RequestContext.class,
    APIGatewayV2HTTPEvent.RequestContext.Authorizer.class,
    APIGatewayV2HTTPEvent.RequestContext.Http.class,
    APIGatewayV2HTTPEvent.RequestContext.IAM.class,
    APIGatewayV2HTTPEvent.RequestContext.CognitoIdentity.class,
    APIGatewayV2HTTPResponse.class,
})
@SerdeImport(APIGatewayV2HTTPEvent.class)
@SerdeImport(APIGatewayV2HTTPEvent.RequestContext.class)
@SerdeImport(APIGatewayV2HTTPEvent.RequestContext.Authorizer.class)
@SerdeImport(APIGatewayV2HTTPEvent.RequestContext.Http.class)
@SerdeImport(APIGatewayV2HTTPEvent.RequestContext.IAM.class)
@SerdeImport(APIGatewayV2HTTPEvent.RequestContext.CognitoIdentity.class)
@SerdeImport(APIGatewayV2HTTPResponse.class)
public class MicronautApiGatewayV2ContainerHandler
    extends MicronautAwsHttpProxyRequestHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    @Inject
    private MicronautApiGatewayV2RequestTransformer<?> requestTransformer;

    @Inject
    private MicronautApiGatewayV2ResponseTransformer responseTransformer;

    @Override
    protected MicronautAwsRequestTransformer<APIGatewayV2HTTPEvent, ?> requestTransformer() {
        return requestTransformer;
    }

    @Override
    protected MicronautAwsResponseTransformer<APIGatewayV2HTTPResponse> responseTransformer() {
        return responseTransformer;
    }

    @Override
    public Class<APIGatewayV2HTTPEvent> inputTypeClass() {
        return APIGatewayV2HTTPEvent.class;
    }

    @Override
    public Class<APIGatewayV2HTTPResponse> outputTypeClass() {
        return APIGatewayV2HTTPResponse.class;
    }
}
