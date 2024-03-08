/*
 * Copyright 2017-2024 original authors
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

import com.amazonaws.services.lambda.runtime.CustomPojoSerializer;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.function.aws.JsonMapperCustomPojoSerializer;
import io.micronaut.serde.ObjectMapper;

import java.util.Collections;

/**
 * Provides an implementation of {@link CustomPojoSerializer} which is loaded via SPI.
 * This implementation avoids paying a double hit on performance when using a serialization library inside the Lambda function.
 * This implementations adds the package {@value #PACKAGE_IO_MICRONAUT_AWS_LAMBDA_EVENTS_SERDE} which contains {@link io.micronaut.serde.annotation.SerdeImport} for the AWS Lambda Events classes to the ObjectMapper creation.
 * @author Sergio del Amo
 * @since 4.0.0
 */
public class SerdeCustomPojoSerializer extends JsonMapperCustomPojoSerializer {

    private static final String PACKAGE_IO_MICRONAUT_AWS_LAMBDA_EVENTS_SERDE = "io.micronaut.aws.lambda.events.serde";

    public SerdeCustomPojoSerializer() {
        this.jsonMapper = instantiateObjectMapper();
    }

    @NonNull
    protected ObjectMapper instantiateObjectMapper() {
        return ObjectMapper.create(Collections.emptyMap(), PACKAGE_IO_MICRONAUT_AWS_LAMBDA_EVENTS_SERDE);
    }

}
