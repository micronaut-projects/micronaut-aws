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
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Duration;
import java.util.Optional;

/**
 * {@link TypeConverter} from {@link Duration} to {@link AttributeValue} using {@link Duration#toString()}.
 *
 * <p>
 * This stores and reads values in DynamoDB as a number, so that they can be sorted numerically as part of a sort key.
 *
 * <p>
 * Durations are stored in the format "[-]X[.YYYYYYYYY]", where X is the number of seconds in the duration, and Y is the number of
 * nanoseconds in the duration, left padded with zeroes to a length of 9. The Y and decimal point may be excluded for durations
 * that are of whole seconds. The duration may be preceded by a - to indicate a negative duration.
 *
 * <p>
 * Examples:
 * <ul>
 *     <li>{@code Duration.ofDays(1)} is stored as {@code ItemAttributeValueMapper.fromNumber("86400")}</li>
 *     <li>{@code Duration.ofSeconds(9)} is stored as {@code ItemAttributeValueMapper.fromNumber("9")}</li>
 *     <li>{@code Duration.ofSeconds(-9)} is stored as {@code ItemAttributeValueMapper.fromNumber("-9")}</li>
 *     <li>{@code Duration.ofNanos(1_234_567_890)} is stored as {@code ItemAttributeValueMapper.fromNumber("1.234567890")}</li>
 *     <li>{@code Duration.ofMillis(1)} is stored as {@code ItemAttributeValueMapper.fromNumber("0.001000000")}</li>
 *     <li>{@code Duration.ofNanos(1)} is stored as {@code ItemAttributeValueMapper.fromNumber("0.000000001")}</li>
 * </ul>
 *
 * @author Sergio del Amo
 * @since 4.0.0
 */
@Singleton
public class DurationToAttributeValueTypeConverter implements TypeConverter<Duration, AttributeValue> {
    @Override
    public Optional<AttributeValue> convert(Duration object, Class<AttributeValue> targetType, ConversionContext context) {
        if (object == null) {
            return Optional.empty();
        }
        return Optional.of(AttributeValue.builder()
            .n(object.getSeconds() +
                (object.getNano() == 0 ? "" : "." + padLeft(9, object.getNano())))
            .build());
    }

    public static String padLeft(int paddingAmount, int valueToPad) {
        String result;
        String value = Integer.toString(valueToPad);
        if (value.length() == paddingAmount) {
            result = value;
        } else {
            int padding = paddingAmount - value.length();
            StringBuilder sb = new StringBuilder(paddingAmount);
            for (int i = 0; i < padding; i++) {
                sb.append('0');
            }
            result = sb.append(value).toString();
        }
        return result;
    }
}
