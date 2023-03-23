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
package io.micronaut.aws.function.apigatewayproxy;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.MutableHttpHeaders;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Adapts headers from a APIGatewayProxyRequestEvent or APIGatewayProxyResponseEvent to Micronaut HttpHeaders.
 *
 * @author Tim Yates
 * @since 4.0.0
 */
public class MultiMutableHttpHeaders implements MutableHttpHeaders {
    public static final String COMMA = ",";
    private final MutableMultiValue values;
    public MultiMutableHttpHeaders(ConversionService conversionService) {
        this(conversionService, Collections.emptyMap(), Collections.emptyMap());
    }

    public MultiMutableHttpHeaders(ConversionService conversionService,
                                   @Nullable Map<String, List<String>> multiValueHeaders,
                                   @Nullable Map<String, String> singleHeaders) {
        values = new MutableMultiValue(conversionService, multiValueHeaders, singleHeaders);
    }

    @Override
    public List<String> getAll(CharSequence name) {
        return values.getAll(name);
    }

    @Nullable
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

    @Override
    public MutableHttpHeaders add(CharSequence name, CharSequence value) {
        values.add(name, value);
        return this;
    }

    @Override
    public MutableHttpHeaders remove(CharSequence header) {
        values.remove(header);
        return this;
    }

    @Override
    public void setConversionService(ConversionService conversionService) {
        this.values.setConversionService(conversionService);
    }

    @NonNull
    public Map<String, List<String>> getMulti() {
        Map<String, List<String>> result = new HashMap<>();
        for (String key : values.names()) {
            result.put(key, values.getAll(key));
        }
        return result;
    }

    public Map<String, String> getSingle() {
        Map<String, String> result = new HashMap<>();
        for (String key : values.names()) {
            result.put(key, String.join(COMMA, values.getAll(key)));
        }
        return result;
    }
}
