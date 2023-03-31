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
import io.micronaut.core.annotation.NonNull;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Map;

/**
 * Utility to map a composite key in a Dynamo DB single table design.
 * @author Sergio del Amo
 * @since 4.0.0
 */
public interface CompositeKey {

    String KEY_PK = "pk";
    String KEY_SK = "sk";

    /**
     *
     * @return Primary Key
     */
    @NonNull
    String getPk();

    /**
     *
     * @return Sort Key
     */
    @NonNull
    String getSk();

    /**
     *
     * @return Composite Key Item representation.
     */
    @NonNull
    default Map<String, AttributeValue> getKey() {
        return Map.of(KEY_PK, AttributeValueUtils.s(getPk()), KEY_SK, AttributeValueUtils.s(getSk()));
    }
}
