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

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Optional;

import io.micronaut.core.util.StringUtils;

public class LambdaUtils {
    private LambdaUtils() {

    }

    public static String decodeBody(final String body, final boolean isBase64Encoded) {
        if (isBase64Encoded) {
            byte[] decodedBytes = Base64.getDecoder().decode(body);
            return new String(decodedBytes, StandardCharsets.UTF_8);
        }

        return body;
    }

    public static String encodeBody(final String body, final boolean isBase64Encoded) {
        return Optional.ofNullable(body)
            .filter(StringUtils::isNotEmpty)
            .map(b -> encodeBody(body.getBytes(StandardCharsets.UTF_8), isBase64Encoded))
            .orElse(null);
    }

    public static String encodeBody(final byte[] body, final boolean isBase64Encoded) {
        if (isBase64Encoded) {
            return Base64.getEncoder().encodeToString(body);
        }

        return new String(body);
    }
}
