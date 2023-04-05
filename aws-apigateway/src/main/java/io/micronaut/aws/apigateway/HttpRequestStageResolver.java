/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.aws.apigateway;

import com.amazonaws.serverless.proxy.RequestReader;
import com.amazonaws.serverless.proxy.model.AwsProxyRequestContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import jakarta.inject.Singleton;

import java.util.Optional;

/**
 * Amazon API Gateway Stage resolvers for an HTTP Request.
 * @author Sergio del Amo
 * @since 3.10.0
 */
@Requires(classes = {AwsProxyRequestContext.class, RequestReader.class, HttpRequest.class})
@Singleton
public class HttpRequestStageResolver implements StageResolver<HttpRequest<?>> {
    @Override
    @NonNull
    public Optional<String> resolve(@NonNull HttpRequest<?> request) {
        return Optional.of(request)
            .flatMap(req -> req.getAttribute(RequestReader.API_GATEWAY_CONTEXT_PROPERTY, AwsProxyRequestContext.class))
            .map(AwsProxyRequestContext::getStage);
    }
}
