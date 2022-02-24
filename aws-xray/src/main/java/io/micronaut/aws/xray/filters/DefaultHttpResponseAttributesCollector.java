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

import com.amazonaws.xray.entities.Entity;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpResponse;

import jakarta.inject.Singleton;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * {@link io.micronaut.context.annotation.DefaultImplementation} of {@link HttpResponseAttributesCollector}.
 *
 * @author Sergio del Amo
 * @since 3.2.0
 */
@Singleton
public class DefaultHttpResponseAttributesCollector implements HttpResponseAttributesCollector {

    /**
     *
     * @param response The HTTP Response
     * @return {@link ErrorCategory#THROTTLE} if 429, {@link ErrorCategory#FAULT} for other 4xx responses, {@link ErrorCategory#ERROR} for 5xx responses.
     */
    @NonNull
    public Optional<ErrorCategory> parseErrorCategory(@NonNull HttpResponse<?> response) {
        int responseCode = response.status().getCode();
        if (responseCode == 429) {
            return Optional.of(ErrorCategory.THROTTLE);
        }
        if (responseCode / 100 == 4) {
            return Optional.of(ErrorCategory.FAULT);
        }
        if (responseCode / 100 == 5) {
            return Optional.of(ErrorCategory.ERROR);
        }
        return Optional.empty();
    }

    /**
     *
     * @param httpResponse HTTP Response
     * @return Map with information about the HTTP Response. It includes information about the HTTP status code and the content length.
     */
    @Override
    @NonNull
    public Map<String, Object> responseAttributes(@NonNull HttpResponse<?> httpResponse) {
        Map<String, Object> responseAttributes = new HashMap<>();
        int responseCode = httpResponse.status().getCode();
        responseAttributes.put("status", responseCode);
        long contentLength = httpResponse.getContentLength();
        if (contentLength != -1L) {
            responseAttributes.put("content_length", contentLength);
        }
        return responseAttributes;
    }

    @Override
    public void populateEntityWithResponse(@NonNull Entity entity, @NonNull HttpResponse<?> response) {
        parseErrorCategory(response).ifPresent(errorCategory -> {
            switch (errorCategory) {
                case THROTTLE:
                    entity.setThrottle(true);
                    entity.setFault(true);
                    break;
                case FAULT:
                    entity.setFault(true);
                    break;
                case ERROR:
                    entity.setError(true);
                    break;
                default:
                    break;
            }
        });
        Map<String, Object> responseAttributes = responseAttributes(response);
        entity.putHttp("response", responseAttributes);
    }
}
