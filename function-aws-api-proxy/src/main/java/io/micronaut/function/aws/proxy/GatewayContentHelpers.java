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

import java.util.Set;

/**
 * Helper methods for API Gateway content.
 */
@Internal
public final class GatewayContentHelpers {

    private static final Set<String> BINARY_CONTENT_TYPES = Set.of("application/octet-stream", "image/jpeg", "image/png", "image/gif");

    private GatewayContentHelpers() {
    }

    /**
     * @param contentType The content type
     * @return True if the content type is encoded as binary
     */
    public static boolean isBinary(String contentType) {
        if (contentType != null) {
            int semidx = contentType.indexOf(';');
            if (semidx > -1) {
                return BINARY_CONTENT_TYPES.contains(contentType.substring(0, semidx).trim());
            } else {
                return BINARY_CONTENT_TYPES.contains(contentType.trim());
            }
        }
        return false;
    }
}
