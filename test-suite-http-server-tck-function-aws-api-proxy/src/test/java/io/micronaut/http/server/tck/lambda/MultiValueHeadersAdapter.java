/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.http.server.tck.lambda;

import com.amazonaws.serverless.proxy.model.Headers;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpHeaders;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.Set;

/**
 * Adapt between {com.amazonaws.serverless.proxy.model.Headers} to  {@link HttpHeaders}
 */
public class MultiValueHeadersAdapter implements HttpHeaders {
    private final Headers multiValueHeaders;
    public MultiValueHeadersAdapter(Headers headers) {
        this.multiValueHeaders = headers;
    }

    @Override
    public List<String> getAll(CharSequence name) {
        if (StringUtils.isNotEmpty(name)) {
            final List<String> strings = multiValueHeaders.get(name.toString());
            if (CollectionUtils.isNotEmpty(strings)) {
                return strings;
            }
        }
        return Collections.emptyList();
    }

    @Nullable
    @Override
    public String get(CharSequence name) {
        if (StringUtils.isNotEmpty(name)) {
            return multiValueHeaders.getFirst(name.toString());
        }
        return null;
    }

    @Override
    public Set<String> names() {
        return multiValueHeaders.keySet();
    }

    @Override
    public Collection<List<String>> values() {
        return multiValueHeaders.values();
    }

    @Override
    public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
        final String v = get(name);
        if (v != null) {
            return ConversionService.SHARED.convert(v, conversionContext);
        }
        return Optional.empty();
    }
}
