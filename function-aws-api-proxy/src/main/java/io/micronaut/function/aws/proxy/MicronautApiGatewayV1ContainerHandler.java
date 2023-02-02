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

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.TypeHint;
import io.micronaut.function.aws.MicronautRequestHandler;
import io.micronaut.http.HttpRequest;
import io.micronaut.serde.annotation.SerdeImport;
import jakarta.inject.Inject;

@TypeHint(
    accessType = {
        TypeHint.AccessType.ALL_DECLARED_CONSTRUCTORS,
        TypeHint.AccessType.ALL_PUBLIC
    },
    value = MicronautApiGatewayV1ContainerHandler.class
)
@Introspected(
    accessKind = {
        Introspected.AccessKind.METHOD,
        Introspected.AccessKind.FIELD,
    },
    visibility = {
        Introspected.Visibility.DEFAULT,
        Introspected.Visibility.PUBLIC
    },
    classes = {
        MicronautApiGatewayV1ContainerHandler.class,
        APIGatewayProxyRequestEvent.class,
        APIGatewayProxyRequestEvent.ProxyRequestContext.class,
        APIGatewayProxyRequestEvent.RequestIdentity.class,
        APIGatewayProxyResponseEvent.class
    })
@SerdeImport(APIGatewayProxyRequestEvent.class)
@SerdeImport(APIGatewayProxyRequestEvent.ProxyRequestContext.class)
@SerdeImport(APIGatewayProxyRequestEvent.RequestIdentity.class)
@SerdeImport(APIGatewayProxyResponseEvent.class)
public class MicronautApiGatewayV1ContainerHandler
    extends MicronautAwsHttpProxyRequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Inject
    private MicronautAwsApiGatewayRequestTransformer requestTransformer;

    @Inject
    private MicronautApiGatewayResponseTransformer<?> responseTransformer;

    @Override
    protected MicronautAwsRequestTransformer<APIGatewayProxyRequestEvent, ? extends HttpRequest<?>> requestTransformer() {
        return requestTransformer;
    }

    @Override
    protected MicronautAwsResponseTransformer<APIGatewayProxyResponseEvent> responseTransformer() {
        return responseTransformer;
    }

    @Override
    public Class<APIGatewayProxyRequestEvent> inputTypeClass() {
        return APIGatewayProxyRequestEvent.class;
    }

    @Override
    public Class<APIGatewayProxyResponseEvent> outputTypeClass() {
        return APIGatewayProxyResponseEvent.class;
    }
}
