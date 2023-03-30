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

import io.micronaut.core.convert.ConversionContext;
import io.micronaut.core.convert.TypeConverter;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.DateTimeException;
import java.time.ZoneOffset;
import java.util.Optional;

/**
 * {@link TypeConverter} from {@link AttributeValue} to {@link ZoneOffset} using {@link ZoneOffset#of(String)}.
 * @author Sergio del Amo
 * @since 4.0.0
 */
@Singleton
public class AttributeValueToZoneOffsetTypeConverter implements TypeConverter<AttributeValue, ZoneOffset> {
    private static final Logger LOG = LoggerFactory.getLogger(AttributeValueToZoneOffsetTypeConverter.class);

    @Override
    public Optional<ZoneOffset> convert(AttributeValue object, Class<ZoneOffset> targetType, ConversionContext context) {
        if (object == null) {
            return Optional.empty();
        }
        String value = object.s();
        try {
            return Optional.of(ZoneOffset.of(value));
        } catch (DateTimeException e) {
            if (LOG.isWarnEnabled()) {
                LOG.warn("Could not parse {} to ZoneId", value);
            }
            return Optional.empty();
        }
    }
}