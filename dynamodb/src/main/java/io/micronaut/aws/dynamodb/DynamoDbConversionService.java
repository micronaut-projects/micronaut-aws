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

import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.beans.BeanWrapper;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;
import io.micronaut.core.beans.exceptions.IntrospectionException;
import java.util.Map;

/**
 * @author Sergio del Amo
 * @since 4.0.0
 * @param <S> The source Type
 */
@DefaultImplementation(DefaultDynamoDbConversionService.class)
public interface DynamoDbConversionService<S> {
    @NonNull
    Map<String, AttributeValue> convert(@NonNull BeanWrapper<S> wrapper);

    @NonNull
    default Map<String, AttributeValue> convert(@NonNull S object) throws IntrospectionException {
        return convert(BeanWrapper.getWrapper(object));
    }

    @NonNull
    <T> T convert(@NonNull Map<String, AttributeValue> item, Class<T> targetType);
}
