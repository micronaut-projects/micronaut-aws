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
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.CaseInsensitiveMutableHttpHeaders;
import io.micronaut.http.MutableHttpHeaders;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Header implementation that preserves multi-value headers for iterable and array conversions.
 */
@Internal
final class MultiValueMutableHttpHeaders implements MutableHttpHeaders {

    private final CaseInsensitiveMutableHttpHeaders delegate;
    private ConversionService conversionService;

    MultiValueMutableHttpHeaders(Map<String, List<String>> headers, ConversionService conversionService) {
        this.delegate = new CaseInsensitiveMutableHttpHeaders(headers, conversionService);
        this.conversionService = conversionService;
    }

    @Override
    public List<String> getAll(CharSequence name) {
        return delegate.getAll(name);
    }

    @Override
    public String get(CharSequence name) {
        return delegate.get(name);
    }

    @Override
    public Set<String> names() {
        return delegate.names();
    }

    @Override
    public Collection<List<String>> values() {
        return delegate.values();
    }

    @Override
    public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
        List<String> valuesForName = getAll(name);
        if (CollectionUtils.isEmpty(valuesForName)) {
            return Optional.empty();
        }
        Object value = isIterableOrArray(conversionContext) && valuesForName.size() > 1 ? valuesForName : valuesForName.get(0);
        return conversionService.convert(value, conversionContext);
    }

    @Override
    public MutableHttpHeaders add(CharSequence header, CharSequence value) {
        delegate.add(header, value);
        return this;
    }

    @Override
    public MutableHttpHeaders remove(CharSequence header) {
        delegate.remove(header);
        return this;
    }

    @Override
    public void setConversionService(ConversionService conversionService) {
        delegate.setConversionService(conversionService);
        this.conversionService = conversionService;
    }

    private static boolean isIterableOrArray(ArgumentConversionContext<?> conversionContext) {
        Class<?> type = conversionContext.getArgument().getType();
        return type.isArray() || Iterable.class.isAssignableFrom(type);
    }
}
