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

import io.micronaut.aws.dynamodb.CompositeKey;
import io.micronaut.core.annotation.NonNull;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

/**
 * Utility class to work with {@link CompositeKey}.
 * @author Sergio del Amo
 * @since 4.0.0
 */
public final class CompositeKeyUtils {

    public static final String KEY_PK = "pk";
    public static final String KEY_SK = "sk";

    private CompositeKeyUtils() {

    }

    /**
     *
     * @return Composite Key Item representation.
     */
    @NonNull
    Map<String, AttributeValue> getKey(@NonNull CompositeKey compositeKey) {
        return Map.of(KEY_PK, AttributeValueUtils.s(compositeKey.getPk()), KEY_SK, AttributeValueUtils.s(compositeKey.getSk()));
    }
}
