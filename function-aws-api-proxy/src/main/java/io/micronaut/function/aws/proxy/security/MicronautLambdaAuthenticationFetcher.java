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
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * An authentication fetcher for {@link CognitoAuthorizerClaims}.
 *
 * @author graemerocher
 * @since 1.1
 */
@Singleton
@Requires(classes = AuthenticationFetcher.class)
public class MicronautLambdaAuthenticationFetcher implements AuthenticationFetcher {

    /**
     * @see <a href="https://tools.ietf.org/html/rfc7519#section-4.1">Registered Claims Names</a>
     */
    protected final static List<String> REGISTERED_CLAIMS_NAMES = Arrays.asList("iss", "sub", "exp", "nbf", "iat", "jti", "aud");

    /**
     * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims">Standard Claims</a>
     */
    protected final static List<String> ID_TOKEN_STANDARD_CLAIMS_NAMES = Arrays.asList(
            "name",
            "given_name",
            "family_name",
            "middle_name",
            "nickname",
            "preferred_username",
            "profile",
            "picture",
            "website",
            "email",
            "email_verified",
            "gender",
            "birthdate",
            "zoneinfo",
            "locale",
            "phone_number",
            "phone_number_verified",
            "address",
            "updated_at",
            "auth_time",
            "nonce",
            "acr",
            "amr",
            "azp");


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
                return Flowable.just(
                        new DefaultAuthentication(
                                authorizer.getPrincipalId(),
                                attributesOfClaims(claims)
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

    /**
     * @see <a href="https://tools.ietf.org/html/rfc7519#section-4.1">Registered Claims Names</a>
     * @see <a href="https://openid.net/specs/openid-connect-core-1_0.html#StandardClaims">Standard Claims</a>
     * @param claims Cognito Claims
     * @return
     */
    protected Map<String, Object> attributesOfClaims(CognitoAuthorizerClaims claims) {

        Map<String, Object> attributes = new HashMap<>();
        attributes.put("sub", claims.getSubject());
        attributes.put("aud", claims.getAudience());
        attributes.put("iss", claims.getIssuer());
        attributes.put("token_use", claims.getTokenUse());
        attributes.put("cognito:username", claims.getUsername());
        attributes.put("preferred_username", claims.getUsername());
        attributes.put("email", claims.getEmail());
        attributes.put("email_verified", claims.isEmailVerified());
        attributes.put("auth_time", claims.getAuthTime());
        attributes.put("iat", claims.getIssuedAt());
        attributes.put("exp", claims.getExpiration());

        for(String claim : Stream.concat(ID_TOKEN_STANDARD_CLAIMS_NAMES.stream(), REGISTERED_CLAIMS_NAMES.stream())
                .collect(Collectors.toList())) {
            String value = claims.getClaim(claim);
            if (value != null) {
                attributes.putIfAbsent(claim, value);
            }
        }
        
        return attributes;
    }

}
