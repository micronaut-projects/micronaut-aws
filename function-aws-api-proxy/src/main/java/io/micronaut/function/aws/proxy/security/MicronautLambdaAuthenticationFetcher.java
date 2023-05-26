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
package io.micronaut.function.aws.proxy.security;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.async.publisher.Publishers;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.filters.AuthenticationFetcher;
import io.micronaut.servlet.http.ServletHttpRequest;
import jakarta.inject.Singleton;
import org.reactivestreams.Publisher;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * An authentication fetcher for Request Context Authorizer.
 *
 * @author sdelamo
 * @since 4.0.0
 */
@Experimental
@Singleton
@Requires(classes = AuthenticationFetcher.class)
public class MicronautLambdaAuthenticationFetcher implements AuthenticationFetcher {
    private static final String HEADER_OIDC_IDENTITY = "x-amzn-oidc-identity";
    private static final String CLAIM_SUB = "sub";
    private static final String CLAIMS = "claims";

    @Override
    public Publisher<Authentication> fetchAuthentication(HttpRequest<?> request) {
        Optional<Authentication> optionalAuthentication = Optional.empty();
        if (request instanceof ServletHttpRequest servletHttpRequest) {
            Object nativeRequest = servletHttpRequest.getNativeRequest();
            if (nativeRequest instanceof APIGatewayV2HTTPEvent aPIGatewayV2HTTPEvent) {
                optionalAuthentication = fetchAuthentication(aPIGatewayV2HTTPEvent);
            } else if (nativeRequest instanceof APIGatewayProxyRequestEvent apiGatewayProxyRequestEvent) {
                optionalAuthentication = fetchAuthentication(apiGatewayProxyRequestEvent);
            }
        }
        if (optionalAuthentication.isEmpty()) {
            optionalAuthentication = fetchAuthenticationFromHeader(request);
        }
        return optionalAuthentication.map(Publishers::just).orElseGet(Publishers::empty);
    }

    private Optional<Authentication> fetchAuthenticationFromHeader(HttpRequest<?> request) {
        final String v = request.getHeaders().get(HEADER_OIDC_IDENTITY);
        if (v != null) {
            return Optional.of(Authentication.build(
                    v,
                    Collections.emptyMap()
                )
            );
        }
        return Optional.empty();
    }

    private Optional<Authentication> fetchAuthentication(APIGatewayProxyRequestEvent request) {
        APIGatewayProxyRequestEvent.ProxyRequestContext proxyRequestContext = request.getRequestContext();
        Map<String, Object> authorizer = proxyRequestContext.getAuthorizer();
        if (authorizer.containsKey(CLAIM_SUB)) {
            return fetchAuthentication(authorizer);
        } else if (authorizer.containsKey(CLAIMS)) {
            Object object = authorizer.get(CLAIMS);
            if (object instanceof Map) {
                return fetchAuthentication((Map<String, Object>) object);
            }
        }
        return Optional.empty();
    }

    private Optional<Authentication> fetchAuthentication(APIGatewayV2HTTPEvent request) {
        if (request.getRequestContext() != null) {
            APIGatewayV2HTTPEvent.RequestContext.Authorizer authorizer = request.getRequestContext().getAuthorizer();
            if (authorizer != null) {
                APIGatewayV2HTTPEvent.RequestContext.Authorizer.JWT jwt = authorizer.getJwt();
                if (jwt != null) {
                    if (jwt.getClaims() != null) {
                        Map<String, Object> claims = new HashMap<>(jwt.getClaims());
                        Object subject = claims.get(CLAIM_SUB);
                        if (subject != null) {
                            return Optional.of(Authentication.build(subject.toString(), claims));
                        }
                    }
                }
            }
        }
        return Optional.empty();
    }

    @NonNull
    private Optional<Authentication> fetchAuthentication(@NonNull Map<String, Object> claims) {
        Object subject = claims.get(CLAIM_SUB);
        if (subject != null) {
            return Optional.of(Authentication.build(subject.toString(), claims));
        }
        return Optional.empty();
    }
}
