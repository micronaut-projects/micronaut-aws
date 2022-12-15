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

import io.micronaut.core.annotation.NonNull;

/**
 * Utility class to use when working with Amazon API Gateway.
 * @see <a href="https://docs.aws.amazon.com/apigateway/latest/developerguide/how-to-deploy-api.html">Deploying a REST API in Amazon API Gateway</a>
 * @author Sergio del Amo
 * @since 3.10.0
 */
public final class AmazonApiGatewayUtils {
    private static final String HTTPS = "https://";
    private static final String EXECUTE_API_SUBDOMAIN = ".execute-api.";
    private static final String DOMAIN_AMAZONAWS_COM = ".amazonaws.com";

    private AmazonApiGatewayUtils() {
    }

    /**
     *
     * @param host the request host. For example obtained via HttpHostResolver API.
     * @return whether the host matches API Gateway's default domain name format `https://{restapi-id}.execute-api.{region}.amazonaws.com/{stageName}`.
     */
    public static boolean isAmazonApiGatewayHost(@NonNull String host) {
        return host.startsWith(HTTPS) &&
            host.endsWith(DOMAIN_AMAZONAWS_COM) &&
            host.contains(EXECUTE_API_SUBDOMAIN);
    }
}
