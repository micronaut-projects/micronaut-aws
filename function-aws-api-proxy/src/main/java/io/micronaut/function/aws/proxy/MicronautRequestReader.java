/*
 * Copyright 2017-2020 original authors
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

import com.amazonaws.serverless.exceptions.InvalidRequestEventException;
import com.amazonaws.serverless.proxy.RequestReader;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.ContainerConfig;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.databind.JsonNode;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.core.type.Argument;
import io.micronaut.core.type.TypeVariableResolver;
import io.micronaut.http.HttpAttributes;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.web.router.UriRoute;
import io.micronaut.web.router.UriRouteMatch;

import javax.ws.rs.core.SecurityContext;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Pattern;

/**
 * Implementation of the {@link RequestReader} class for Micronaut.
 *
 * @author graemerocher
 * @since 1.1
 */
@Internal
class MicronautRequestReader extends RequestReader<AwsProxyRequest, MicronautAwsProxyRequest<?>> {

    private final MicronautLambdaContainerContext environment;

    /**
     * Default constructor.
     *
     * @param environment The {@link MicronautLambdaContainerContext}
     */
    MicronautRequestReader(MicronautLambdaContainerContext environment) {
        this.environment = environment;
    }

    @Override
    public MicronautAwsProxyRequest<?> readRequest(
            AwsProxyRequest request,
            SecurityContext securityContext,
            Context lambdaContext,
            ContainerConfig config) throws InvalidRequestEventException {
        try {
            final String path = config.isStripBasePath() ? stripBasePath(request.getPath(), config) : getPathNoBase(request);
            final MicronautAwsProxyRequest<?> containerRequest = new MicronautAwsProxyRequest(
                    path,
                    request,
                    securityContext,
                    lambdaContext,
                    config
            );
            final UriRouteMatch<Object, Object> finalRoute = environment.getRouter().route(
                    containerRequest.getMethod(),
                    containerRequest.getPath()
            ).orElse(null);
            if (finalRoute != null) {
                containerRequest.setAttribute(HttpAttributes.ROUTE_MATCH, finalRoute);
                final UriRoute route = finalRoute.getRoute();
                containerRequest.setAttribute(HttpAttributes.ROUTE, route);
                containerRequest.setAttribute(HttpAttributes.URI_TEMPLATE, route.getUriMatchTemplate().toString());

                final boolean permitsRequestBody = HttpMethod.permitsRequestBody(containerRequest.getMethod());
                if (permitsRequestBody) {
                    final MediaType requestContentType = containerRequest.getContentType().orElse(null);
                    if (requestContentType != null && requestContentType.getExtension().equalsIgnoreCase("json")) {
                        final MediaType[] expectedContentType = finalRoute.getAnnotationMetadata().getValue(Consumes.class, MediaType[].class).orElse(null);
                        if (expectedContentType == null || Arrays.stream(expectedContentType).anyMatch(ct -> ct.getExtension().equalsIgnoreCase("json"))) {
                            final Optional<String> body = containerRequest.getBody(String.class);
                            if (body.isPresent()) {

                                Argument<?> bodyArgument = finalRoute.getBodyArgument().orElse(null);
                                if (bodyArgument == null) {
                                    bodyArgument = Arrays.stream(finalRoute.getArguments()).filter(arg -> HttpRequest.class.isAssignableFrom(arg.getType()))
                                                        .findFirst()
                                                        .flatMap(TypeVariableResolver::getFirstTypeVariable).orElse(null);
                                }

                                if (bodyArgument != null) {
                                    final Class<?> rawType = bodyArgument.getType();
                                    if (Publishers.isConvertibleToPublisher(rawType) || HttpRequest.class.isAssignableFrom(rawType)) {
                                        bodyArgument = bodyArgument.getFirstTypeVariable().orElse(Argument.OBJECT_ARGUMENT);
                                    }
                                    final Object decoded = environment.getJsonCodec().decode(bodyArgument, body.get());
                                    ((MicronautAwsProxyRequest) containerRequest)
                                            .setDecodedBody(decoded);
                                } else {
                                    final JsonNode jsonNode = environment.getJsonCodec().decode(JsonNode.class, body.get());
                                    ((MicronautAwsProxyRequest) containerRequest)
                                            .setDecodedBody(jsonNode);
                                }
                            }
                        }
                    }
                }
            }
            return containerRequest;
        } catch (Exception e) {
            throw new InvalidRequestEventException("Invalid Request: " + e.getMessage(), e);
        }
    }

    @Override
    protected Class<? extends AwsProxyRequest> getRequestClass() {
        return AwsProxyRequest.class;
    }

    private static String getPathNoBase(AwsProxyRequest request) {
        if (request.getResource() == null || "".equals(request.getResource())) {
            return request.getPath();
        }

        if (request.getPathParameters() == null || request.getPathParameters().isEmpty()) {
            return request.getResource();
        }

        String path = request.getResource();
        for (Map.Entry<String, String> variable : request.getPathParameters().entrySet()) {
            path = path.replaceAll("\\{" + Pattern.quote(variable.getKey()) + "\\+?}", variable.getValue());
        }

        return path;
    }
}
