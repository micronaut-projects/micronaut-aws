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
package io.micronaut.function.aws.proxy;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.convert.value.ConvertibleMultiValues;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.ArrayList;
import java.util.HashMap;

/**
 *
 */
public class MultiValue implements ConvertibleMultiValues<String> {

    protected Map<String, List<String>> values;
    protected ConversionService conversionService;

    public MultiValue(ConversionService conversionService, Map<String, List<String>> multi, Map<String, String> single) {
        this.conversionService = conversionService;
        if (multi == null && single == null) {
            values = Collections.emptyMap();
        } else {
            values = new HashMap<>();
            if (multi != null) {
                for (String name : multi.keySet()) {
                    values.computeIfAbsent(name, s -> new ArrayList<>());
                    values.get(name).addAll(multi.get(name));
                }
            }
            if (CollectionUtils.isNotEmpty(single)) {
                for (String name : single.keySet()) {
                    values.computeIfAbsent(name, s -> new ArrayList<>());
                    String value = single.get(name);
                    if (!values.get(name).contains(value)) {
                        values.get(name).add(value);
                    }
                }
            }
        }
    }

    @Override
    public List<String> getAll(CharSequence name) {
        String headerName = HttpHeaderUtils.normalizeHttpHeaderCase(name.toString());
        return getAllIgnoreCase(headerName)
            .orElse(Collections.emptyList());
    }

    @Nullable
    @Override
    public String get(CharSequence name) {
        List<String> values = getAll(name);
        if (CollectionUtils.isEmpty(values)) {
            return null;
        }
        return values.get(0);
    }

    @Override
    public Set<String> names() {
        return values.keySet();
    }

    @Override
    public Collection<List<String>> values() {
        return values.values();
    }

    @Override
    public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
        final String v = get(name);
        if (v != null) {
            return conversionService.convert(v, conversionContext);
        }
        return Optional.empty();
    }

    @NonNull
    private Optional<List<String>> getAllIgnoreCase(@Nullable String headerName) {
        if (StringUtils.isEmpty(headerName)) {
            return Optional.empty();
        }
        List<String> l = values.get(headerName);
        if (l != null) {
            return Optional.of(l);
        }
        for (String k : values.keySet()) {
            if (k.equalsIgnoreCase(headerName)) {
                l = values.get(k);
                if (l != null) {
                    return Optional.of(l);
                }
            }
        }
        return Optional.empty();
    }
}
