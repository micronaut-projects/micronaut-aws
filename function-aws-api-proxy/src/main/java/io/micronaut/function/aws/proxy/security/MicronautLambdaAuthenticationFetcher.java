/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.function.aws.proxy.security;

import com.amazonaws.serverless.proxy.model.ApiGatewayAuthorizerContext;
import com.amazonaws.serverless.proxy.model.CognitoAuthorizerClaims;
import io.micronaut.context.annotation.Requires;
import io.micronaut.function.aws.proxy.MicronautAwsProxyRequest;
import io.micronaut.http.HttpRequest;
import io.micronaut.security.authentication.Authentication;
import io.micronaut.security.authentication.DefaultAuthentication;
import io.micronaut.security.filters.AuthenticationFetcher;
import io.reactivex.Flowable;
import org.reactivestreams.Publisher;

import javax.inject.Singleton;
import java.util.Collections;

/**
 * An authentication fetcher for {@link CognitoAuthorizerClaims}.
 *
 * @author graemerocher
 * @since 1.1
 */
@Singleton
@Requires(classes = AuthenticationFetcher.class)
public class MicronautLambdaAuthenticationFetcher implements AuthenticationFetcher {

    public static final String HEADER_OIDC_IDENTITY = "x-amzn-oidc-identity";

    @Override
    public Publisher<Authentication> fetchAuthentication(HttpRequest<?> request) {
        if (request instanceof MicronautAwsProxyRequest) {
            MicronautAwsProxyRequest awsProxyRequest = (MicronautAwsProxyRequest) request;
            final ApiGatewayAuthorizerContext authorizer = awsProxyRequest
                    .getAwsProxyRequest()
                    .getRequestContext()
                    .getAuthorizer();

            if (authorizer != null) {
                final CognitoAuthorizerClaims claims = authorizer.getClaims();
                // TODO: Make a custom authentication
                return Flowable.just(
                        new DefaultAuthentication(
                                authorizer.getPrincipalId(),
                                Collections.emptyMap()
                        )
                );
            } else {
                final String v = request.getHeaders().get(HEADER_OIDC_IDENTITY);
                if (v != null) {
                    return Flowable.just(
                            new DefaultAuthentication(
                                    v,
                                    Collections.emptyMap()
                            )
                    );
                }
            }
        }
        return Flowable.empty();
    }
}
