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
package io.micronaut.aws.lambda.events.serde;

import com.amazonaws.lambda.thirdparty.com.fasterxml.jackson.annotation.JsonProperty;
import com.amazonaws.services.lambda.runtime.events.S3Event;
import io.micronaut.core.annotation.Internal;
import io.micronaut.serde.annotation.SerdeImport;

import java.util.List;

/**
 * {@link SerdeImport} for {@link S3Event}.
 *
 * @author Dan Hollingsworth
 * @since 4.0.0
 */
@SerdeImport(value = S3Event.class, mixin = S3EventSerde.S3EventMixin.class)
@Internal
public class S3EventSerde {

    /**
     * Records Mixin.
     */
    public interface S3EventMixin {
        /**
         * @return Records.
         */
        @JsonProperty("Records") List<?> getRecords();

        /**
         * Set Records.
         */
        @JsonProperty("Records") void setRecords(List<?> records);
    }
}
