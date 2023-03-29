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

import io.micronaut.core.annotation.NonNull;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

/**
 * Utility class to work with {@link AttributeValue} builders.
 */
public final class AttributeValueUtils {

    private AttributeValueUtils() {

    }

    /**
     *
     * @param value Value
     * @return The Attribute value
     */
    @NonNull
    public static AttributeValue s(@NonNull String value) {
        return AttributeValue.builder().s(value).build();
    }

    /**
     *
     * @param value Value
     * @return The Attribute value
     */
    @NonNull
    public static AttributeValue n(@NonNull String value) {
        return AttributeValue.builder().n(value).build();
    }
}
