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

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.Nullable;

/**
 * Global Secondary Index 1.
 * @author Sergio del Amo
 * @since 4.0.0
 */
@Introspected
public interface GlobalSecondaryIndex1 {

    /**
     *
     * @return  Global Secondary Index 1 Primary Key
     */
    @Nullable
    String getGsi1Pk();

    /**
     *
     * @return  Global Secondary Index 2 Sort Key
     */
    @Nullable
    String getGsi1Sk();

}
