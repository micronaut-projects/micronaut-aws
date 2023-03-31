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
package io.micronaut.aws.dynamodb;

import io.micronaut.aws.dynamodb.utils.AttributeValueUtils;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Global Secondary Index 5.
 * @author Sergio del Amo
 * @since 4.0.0
 */
@Introspected
public interface GlobalSecondaryIndex5 {
    String KEY_GSI5_PK = "gsi5pk";
    String KEY_GSI5_SK = "gsi5sk";

    @NonNull
    default Optional<Map<String, AttributeValue>> getGsi5() {
        if (getGsi5Pk() == null && getGsi5Sk() == null) {
            return Optional.empty();
        }
        Map<String, AttributeValue> result = new HashMap<>();
        if (getGsi5Pk() != null) {
            result.put(KEY_GSI5_PK, AttributeValueUtils.s(getGsi5Pk()));
        }
        if (getGsi5Sk() != null) {
            result.put(KEY_GSI5_SK, AttributeValueUtils.s(getGsi5Sk()));
        }
        return Optional.of(result);
    }

    /**
     *
     * @return  Global Secondary Index 5 Primary Key
     */
    @Nullable
    String getGsi5Pk();

    /**
     *
     * @return  Global Secondary Index 5 Sort Key
     */
    @Nullable
    String getGsi5Sk();

}
