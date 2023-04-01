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
package io.micronaut.aws.dynamodb.utils;

import io.micronaut.aws.dynamodb.GlobalSecondaryIndex1;
import io.micronaut.core.annotation.NonNull;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Utility class to work with {@link io.micronaut.aws.dynamodb.GlobalSecondaryIndex1}.
 * @author Sergio del Amo
 * @since 4.0.0
 */
public final class GlobalSecondaryIndex1Utils {
    public static final String KEY_GSI1_PK = "gsi1pk";
    public static final String KEY_GSI1_SK = "gsi1sk";

    private GlobalSecondaryIndex1Utils() {
    }

    @NonNull
    public static Optional<Map<String, AttributeValue>> getGsi1(@NonNull GlobalSecondaryIndex1 gsi) {
        if (gsi.getGsi1Pk() == null && gsi.getGsi1Sk() == null) {
            return Optional.empty();
        }
        Map<String, AttributeValue> result = new HashMap<>();
        if (gsi.getGsi1Pk() != null) {
            result.put(KEY_GSI1_PK, AttributeValueUtils.s(gsi.getGsi1Pk()));
        }
        if (gsi.getGsi1Sk() != null) {
            result.put(KEY_GSI1_SK, AttributeValueUtils.s(gsi.getGsi1Sk()));
        }
        return Optional.of(result);
    }
}
