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
package io.micronaut.aws.function.apigatewayproxy;

import io.micronaut.core.execution.ExecutionFlow;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.server.RequestLifecycle;
import io.micronaut.http.server.RouteExecutor;
import io.micronaut.web.router.RouteInfo;

import java.util.Optional;

/**
 * Used in {@link ApiGatewayProxyRequestEventHandler} to handle the full route processing lifecycle for a request.
 *
 * @author Sergio del Amo
 * @since 4.0.0
 */
public class AmazonApiGatewayRequestLifecycle extends RequestLifecycle {
    /**
     * @param routeExecutor The route executor to use for route resolution
     * @param request       The request to process
     */
    protected AmazonApiGatewayRequestLifecycle(RouteExecutor routeExecutor, HttpRequest<?> request) {
        super(routeExecutor, request);
    }

    /**
     * @return An ExecutionFlow
     */
    ExecutionFlow<MutableHttpResponse<?>> run() {
        return normalFlow().map(response -> {
            Optional<RouteInfo> routeInfo = response.getAttribute(HttpAttributes.ROUTE_INFO, RouteInfo.class);
            if (routeInfo.isPresent() && (response.getContentType().isEmpty())) {
                routeInfo.get().getAnnotationMetadata().getValue(Produces.class, String.class).ifPresent(response::contentType);
            }
            return response;
        });
    }
}
