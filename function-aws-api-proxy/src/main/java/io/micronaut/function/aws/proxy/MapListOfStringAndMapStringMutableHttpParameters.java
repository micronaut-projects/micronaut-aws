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

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.MutableHttpParameters;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of {@link MutableHttpParameters} that uses a {@link MutableMapListOfStringAndMapStringConvertibleMultiValue} internally.
 */
@Internal
public final class MapListOfStringAndMapStringMutableHttpParameters implements MutableHttpParameters {

    private final MutableMapListOfStringAndMapStringConvertibleMultiValue values;

    public MapListOfStringAndMapStringMutableHttpParameters(ConversionService conversionService,
                                                     Map<String, List<String>> multiValue,
                                                     Map<String, String> single) {
        this.values = new MutableMapListOfStringAndMapStringConvertibleMultiValue(conversionService, multiValue, single);
    }

    @Override
    public MutableHttpParameters add(CharSequence name, List<CharSequence> valuesToBeAddded) {
        values.add(name, valuesToBeAddded);
        return this;
    }

    @Override
    public void setConversionService(ConversionService conversionService) {
        values.setConversionService(conversionService);
    }

    @Override
    public List<String> getAll(CharSequence name) {
        return values.getAll(name);
    }

    @Override
    public String get(CharSequence name) {
        return values.get(name);
    }

    @Override
    public Set<String> names() {
        return values.names();
    }

    @Override
    public Collection<List<String>> values() {
        return values.values();
    }

    @Override
    public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
        final String v = get(name);
        if (v != null) {
            return values.getConversionService().convert(v, conversionContext);
        }
        return Optional.empty();
    }
}
