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

import io.micronaut.core.beans.BeanIntrospection;
import io.micronaut.core.beans.BeanIntrospector;
import io.micronaut.core.beans.BeanProperty;
import io.micronaut.core.beans.BeanWrapper;
import io.micronaut.core.beans.exceptions.IntrospectionException;
import io.micronaut.core.convert.ConversionService;
import jakarta.inject.Singleton;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * {@link io.micronaut.context.annotation.DefaultImplementation} of {@link DynamoDbConversionService} which uses {@link ConversionService} to convert from and to {@link AttributeValue} map.
 *
 */
@Singleton
public class DefaultDynamoDbConversionService implements DynamoDbConversionService {

    private final ConversionService conversionService;

    public DefaultDynamoDbConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }

    @Override
    public <S> Map<String, AttributeValue> convert(BeanWrapper<S> wrapper) {
        Map<String, AttributeValue> result = new HashMap<>();
        S bean = wrapper.getBean();
        for (BeanProperty<S, ?> beanProperty : wrapper.getBeanProperties()) {
            Optional<AttributeValue> attributeValueOptional = conversionService.convert(beanProperty.get(bean), AttributeValue.class);
            if (attributeValueOptional.isPresent()) {
                AttributeValue attributeValue = attributeValueOptional.get();
                result.put(beanProperty.getName(), attributeValue);
            } else {
                try {
                    BeanWrapper valueWrapper = BeanWrapper.getWrapper(beanProperty.get(bean));
                    Map<String, AttributeValue> valueWrapperMap = convert(valueWrapper);
                    AttributeValue attributeValue = AttributeValue.builder().m(valueWrapperMap).build();
                    result.put(beanProperty.getName(), attributeValue);
                } catch (IntrospectionException e) {

                }
            }
        }
        return result;
    }

    @Override
    public <T> T convert(Map<String, AttributeValue> item, Class<T> targetType) {
        final BeanIntrospection<T> introspection = BeanIntrospection.getIntrospection(targetType);
        Object[] arguments = new Object[introspection.getConstructorArguments().length];
        int counter = 0;
        for (BeanProperty beanProperty : introspection.getBeanProperties()) {
            if (item.containsKey(beanProperty.getName())) {
                if (BeanIntrospector.SHARED.findIntrospection(beanProperty.getType()).isPresent()) {
                    Map<String, AttributeValue> m = item.get(beanProperty.getName()).m();
                    if (m != null) {
                        arguments[counter++] = convert(m, beanProperty.getType());
                    }
                } else {
                    arguments[counter++] = conversionService.convert(item.get(beanProperty.getName()), beanProperty.getType())
                        .orElse(null);
                }
            } else {
                arguments[counter++] = null;
            }
        }
        return introspection.instantiate(arguments);
    }
}
