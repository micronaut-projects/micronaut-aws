/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.aws.function.apigatewayproxy;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleMultiValues;
import io.micronaut.core.util.ArgumentUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation for Project.fn of a multi value map.
 *
 * @author graemerocher
 * @since 1.0.0
 */
@Internal
public class ApiGatewayMultiValueMap implements ConvertibleMultiValues<String> {
    protected ConversionService conversionService;
    private final Map<String, List<String>> map;

    /**
     * Default constructor.
     *
     * @param map               The target map. Never null
     * @param conversionService The conversion service
     */
    public ApiGatewayMultiValueMap(Map<String, List<String>> map, ConversionService conversionService) {
        this.map = Objects.requireNonNull(map, "Passed map cannot be null");
        this.conversionService = Objects.requireNonNull(conversionService, "ConversionService cannot be null");
    }

    @Override
    public List<String> getAll(CharSequence name) {
        ArgumentUtils.requireNonNull("name", name);
        return map.getOrDefault(name.toString(), Collections.emptyList());
    }

    @Nullable
    @Override
    public String get(CharSequence name) {
        ArgumentUtils.requireNonNull("name", name);

        final List<String> values = map.get(name.toString());
        if (values != null) {
            final Iterator<String> i = values.iterator();
            if (i.hasNext()) {
                return i.next();
            }
        }
        return null;
    }

    @Override
    public Set<String> names() {
        return map.keySet();
    }

    @Override
    public Collection<List<String>> values() {
        return map.values();
    }

    @Override
    public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
        final String v = get(name);
        if (v != null) {
            return conversionService.convert(v, conversionContext);
        }
        return Optional.empty();
    }

    /**
     * @param conversionService The conversion service.
     */
    public void setConversionService(@NonNull ConversionService conversionService) {
        this.conversionService = conversionService;
    }
}
