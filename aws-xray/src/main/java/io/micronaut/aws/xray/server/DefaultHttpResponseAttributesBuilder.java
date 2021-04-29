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

import com.amazonaws.xray.entities.Entity;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpResponse;

import javax.inject.Singleton;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Sergio del Amo
 * @since 2.7.0
 */
@Singleton
public class DefaultHttpResponseAttributesBuilder implements HttpResponseAttributesBuilder {

    @Override
    public void putHttpResponseInformation(@NonNull Entity entity, @NonNull HttpResponse<?> response) {
        if (hasError(response)) {
            entity.setError(true);
        }
        if (hasFault(response)) {
            entity.setFault(true);
        }
        if (hasThrottle(response)) {
            entity.setThrottle(true);
        }
        Map<String, Object> responseAttributes = build(response);
        entity.putHttp("response", responseAttributes);
    }

    private static boolean hasError(@NonNull HttpResponse<?> httpResponse) {
        int responseCode = httpResponse.getStatus().getCode();
        return responseCode / 100 == 4;
    }

    private static boolean hasFault(@NonNull HttpResponse<?> httpResponse) {
        int responseCode = httpResponse.getStatus().getCode();
        return responseCode / 100 == 5;
    }

    private static boolean hasThrottle(@NonNull HttpResponse<?> httpResponse) {
        int responseCode = httpResponse.getStatus().getCode();
        return responseCode / 100 == 429;
    }

    @NonNull
    private static Map<String, Object> build(@NonNull HttpResponse<?> httpResponse) {
        Map<String, Object> responseAttributes = new HashMap<>();
        int responseCode = httpResponse.getStatus().getCode();
        responseAttributes.put("status", responseCode);
        long contentLength = httpResponse.getContentLength();
        if (contentLength != -1L) {
            responseAttributes.put("content_length", contentLength);
        }
        return responseAttributes;
    }
}
