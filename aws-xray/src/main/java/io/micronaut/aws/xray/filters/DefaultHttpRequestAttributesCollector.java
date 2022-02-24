/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.aws.xray.filters;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * {@link io.micronaut.context.annotation.DefaultImplementation} of {@link HttpRequestAttributesCollector}.
 * It collects the request uri, method user_agent, client id.
 * This class is based on {@code AWSXRayServletFilter} and it uses the same keys for the attributes.
 *
 * @see <a href="https://docs.aws.amazon.com/xray-sdk-for-java/latest/javadoc/com/amazonaws/xray/javax/servlet/AWSXRayServletFilter.html">AWSXRayServletFilter</a>.
 * @author Sergio del Amo
 * @since 3.2.0
 */
@Singleton
public class DefaultHttpRequestAttributesCollector implements HttpRequestAttributesCollector {

    public static final String URL = "url";
    public static final String METHOD = "method";
    public static final String USER_AGENT_KEY = "user_agent";
    public static final String CLIENT_IP = "client_ip";
    public static final String X_FORWARDED_FOR_KEY = "x_forwarded_for";
    public static final String X_FORWARDED_FOR = "X-Forwarded-For";
    public static final String USER_AGENT = "User-Agent";

    @Override
    @NonNull
    public Map<String, Object> requestAttributes(@NonNull HttpRequest<?> request) {
        Map<String, Object> requestAttributes = new HashMap<>();
        requestAttributes.put(URL, request.getUri().toString());
        requestAttributes.put(METHOD, request.getMethod());

        getUserAgent(request).ifPresent(s -> requestAttributes.put(USER_AGENT_KEY, s));

        Optional<String> xForwardedFor = getXForwardedFor(request);
        if (xForwardedFor.isPresent()) {
            requestAttributes.put(CLIENT_IP, xForwardedFor.get());
            requestAttributes.put(X_FORWARDED_FOR_KEY, true);
        } else {
            Optional<String> clientIp = getClientIp(request);
            clientIp.ifPresent(s -> requestAttributes.put(CLIENT_IP, s));
        }
        return requestAttributes;
    }

    @NonNull
    private Optional<String> getClientIp(@NonNull HttpRequest<?> request) {
        return Optional.ofNullable(request.getRemoteAddress().toString());
    }

    @NonNull
    private Optional<String> getXForwardedFor(@NonNull HttpRequest<?> request) {
        String forwarded = request.getHeaders().get(X_FORWARDED_FOR);
        if (forwarded != null) {
            return Optional.of(forwarded.split(",")[0].trim());
        }
        return Optional.empty();
    }

    @NonNull
    private Optional<String> getUserAgent(@NonNull HttpRequest<?> request) {
        String userAgentHeaderString = request.getHeaders().get(USER_AGENT);
        if (null != userAgentHeaderString) {
            return Optional.of(userAgentHeaderString);
        }
        return Optional.empty();
    }

}
