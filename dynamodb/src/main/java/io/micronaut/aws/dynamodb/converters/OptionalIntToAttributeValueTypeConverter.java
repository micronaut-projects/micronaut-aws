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
package io.micronaut.aws.dynamodb.converters;

import io.micronaut.aws.dynamodb.utils.AttributeValueUtils;
import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.TypeConverter;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Optional;
import java.util.OptionalInt;

/**
 * {@link TypeConverter} from {@link OptionalInt} to {@link AttributeValue}.
 * @author Sergio del Amo
 * @since 4.0.0
 */
@Singleton
public class OptionalIntToAttributeValueTypeConverter implements TypeConverter<OptionalInt, AttributeValue> {
    @Override
    public Optional<AttributeValue> convert(OptionalInt object, Class<AttributeValue> targetType, ConversionContext context) {
        return object != null && object.isPresent() ?
            Optional.of(AttributeValueUtils.n("" + object.getAsInt())) :
                Optional.empty();
    }
}