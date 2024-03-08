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
package io.micronaut.function.aws;

import com.amazonaws.services.lambda.runtime.CustomPojoSerializer;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.order.OrderUtil;
import io.micronaut.core.type.Argument;
import io.micronaut.json.JsonMapper;
import io.micronaut.json.JsonMapperSupplier;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Type;
import java.util.ServiceLoader;
import java.util.stream.Stream;

/**
 * Provides an implementation of {@link CustomPojoSerializer} which is loaded via SPI. This implementation avoids paying a double hit on performance when using a serialization library inside the Lambda function.
 * @author Sergio del Amo
 * @since 4.0.0
 */
public class JsonMapperCustomPojoSerializer implements CustomPojoSerializer {
    protected JsonMapper jsonMapper;

    public JsonMapperCustomPojoSerializer() {
        this.jsonMapper = createDefault();
    }

    /**
     *
     * @return A JsonMapper
     */
    // https://github.com/micronaut-projects/micronaut-core/pull/9445
    @NonNull
    protected JsonMapper createDefault() {
        return ServiceLoader.load(JsonMapperSupplier.class).stream()
            .flatMap(p -> {
                try {
                    JsonMapperSupplier supplier = p.get();
                    return Stream.ofNullable(supplier);
                } catch (Exception e) {
                    return Stream.empty();
                }
            })
            .sorted(OrderUtil.COMPARATOR)
            .flatMap(jsonMapperSupplier -> {
                try {
                    return Stream.of(jsonMapperSupplier.get());
                } catch (Exception e) {
                    return Stream.empty();
                }
            })
            .findFirst()
            .orElseThrow(() -> new IllegalStateException("No JsonMapper implementation found"));
    }

    /**
     *
     * @return The Json Mapper
     */
    public JsonMapper getJsonMapper() {
        return jsonMapper;
    }

    @Override
    public <T> T fromJson(InputStream input, Type type) {
        try {
            return (T) jsonMapper.readValue(input, Argument.of(type));
        } catch (IOException e) {
            throw new CustomPojoSerializerException(e);
        }
    }

    @Override
    public <T> T fromJson(String input, Type type) {
        try {
            return (T) jsonMapper.readValue(input, Argument.of(type));
        } catch (IOException e) {
            throw new CustomPojoSerializerException(e);
        }
    }

    @Override
    public <T> void toJson(T value, OutputStream output, Type type) {
        Argument<T> argumentType = (Argument<T>) Argument.of(type);
        try {
            jsonMapper.writeValue(output, argumentType, value);
        } catch (IOException e) {
            throw new CustomPojoSerializerException(e);
        }
    }
}
