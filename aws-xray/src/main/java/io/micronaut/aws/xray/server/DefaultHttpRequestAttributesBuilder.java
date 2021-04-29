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
package io.micronaut.aws.xray.server;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * @author Sergio del Amo
 * @since 2.7.0
 */
@Singleton
public class DefaultHttpRequestAttributesBuilder implements HttpRequestAttributesBuilder {

    @Override
    @NonNull
    public Map<String, Object> requestAttributes(@NonNull HttpRequest<?> request) {
        Map<String, Object> requestAttributes = new HashMap<>();
        requestAttributes.put("url", request.getUri().toString());
        requestAttributes.put("method", request.getMethod());

        Optional<String> userAgent = getUserAgent(request);
        userAgent.ifPresent(s -> requestAttributes.put("user_agent", s));

        Optional<String> xForwardedFor = getXForwardedFor(request);
        if (xForwardedFor.isPresent()) {
            requestAttributes.put("client_ip", xForwardedFor.get());
            requestAttributes.put("x_forwarded_for", true);
        } else {
            Optional<String> clientIp = getClientIp(request);
            clientIp.ifPresent(s -> requestAttributes.put("client_ip", s));
        }
        return requestAttributes;
    }

    private Optional<String> getClientIp(HttpRequest<?> request) {
        return Optional.ofNullable(request.getRemoteAddress().toString());
    }

    private Optional<String> getXForwardedFor(HttpRequest<?> request) {
        String forwarded = request.getHeaders().get("X-Forwarded-For");
        if (forwarded != null) {
            return Optional.of(forwarded.split(",")[0].trim());
        }
        return Optional.empty();
    }

    private Optional<String> getUserAgent(HttpRequest<?> request) {
        String userAgentHeaderString = request.getHeaders().get("User-Agent");
        if (null != userAgentHeaderString) {
            return Optional.of(userAgentHeaderString);
        }
        return Optional.empty();
    }

}
