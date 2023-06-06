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
package io.micronaut.function.aws.proxy;

import io.micronaut.core.annotation.Internal;
import jakarta.inject.Singleton;

import java.util.HashSet;
import java.util.Set;

/**
 * Bean to check if response content is binary and should be base 64 encoded
 */
@Internal
@Singleton
public final class BinaryContentConfiguration {

    private final Set<String> binaryContentTypes = new HashSet<>();

    public BinaryContentConfiguration() {
        binaryContentTypes.addAll(Set.of(
                "application/octet-stream",
                "image/jpeg",
                "image/png",
                "image/gif",
                "application/zip"
        ));
    }

    /**
     * Add a content type to the list of binary content types.
     *
     * @param contentType The content type to add
     */
    public void addBinaryContentType(String contentType) {
        binaryContentTypes.add(contentType);
    }

    /**
     * @param contentType The content type
     * @return True if the content type is encoded as binary
     */
    public boolean isBinary(String contentType) {
        if (contentType != null) {
            int semidx = contentType.indexOf(';');
            if (semidx > -1) {
                return binaryContentTypes.contains(contentType.substring(0, semidx).trim());
            } else {
                return binaryContentTypes.contains(contentType.trim());
            }
        }
        return false;
    }
}
