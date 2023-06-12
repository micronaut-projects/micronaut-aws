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
package io.micronaut.function.aws.proxy.test;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.MediaType;
import io.micronaut.json.JsonMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.Optional;
import java.util.function.Supplier;

/**
 * Utility class to provide conversion for HTTP request body.
 * @author Sergio del Amo
 * @since 4.0.0
 */
@Internal
public final class BodyUtils {

    private static final Logger LOG = LoggerFactory.getLogger(BodyUtils.class);

    private BodyUtils() {
    }

    @NonNull
    public static Optional<String> bodyAsString(@NonNull JsonMapper jsonMapper,
                                  @NonNull Supplier<MediaType> contentTypeSupplier,
                                  @NonNull Supplier<Charset> characterEncodingSupplier ,
                                  @NonNull Supplier<Object> bodySupplier) {
        Object body = bodySupplier.get();
        MediaType mediaType = contentTypeSupplier.get();
        boolean mapFromJson = mediaType == null || mediaType.equals(MediaType.APPLICATION_JSON_TYPE);
        if (body instanceof CharSequence) {
            return Optional.of(body.toString());
        } else if (body instanceof byte[] bytes) {
            return Optional.of(new String(bytes, characterEncodingSupplier.get()));
        } else if (mapFromJson) {
            try {
                return Optional.of(jsonMapper.writeValueAsString(body));
            } catch (IOException e) {
                LOG.error("IOException writing body to JSON String", e);
                return Optional.empty();
            }
        }
        return Optional.empty();
    }
}
