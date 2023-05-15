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

import io.micronaut.core.convert.ConversionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A mutable version of {@link MultiValue}.
 */
public class MutableMultiValue extends MultiValue {

    public MutableMultiValue(ConversionService conversionService, Map<String, List<String>> multi, Map<String, String> single) {
        super(conversionService, multi, single);
    }

    /**
     * Adds the given values to the existing values for the given name.
     * @param name
     * @param valuesToBeAdded
     */
    public void add(CharSequence name, List<CharSequence> valuesToBeAdded) {
        String key = HttpHeaderUtils.normalizeHttpHeaderCase(name.toString());
        values.computeIfAbsent(key, s -> new ArrayList<>());
        values.get(key).addAll(valuesToBeAdded.stream().map(CharSequence::toString).toList());
    }

    /**
     * Adds the given value to the existing values for the given name.
     * @param name
     * @param value
     */
    public void add(CharSequence name, CharSequence value) {
        String key = HttpHeaderUtils.normalizeHttpHeaderCase(name.toString());
        values.computeIfAbsent(key, s -> new ArrayList<>());
        values.get(key).add(value.toString());
    }

    /**
     * Removes the given header.
     * @param header
     */
    public void remove(CharSequence header) {
        values.remove(header.toString());
    }

    /**
     * Sets the conversion service.
     * @param conversionService
     */
    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
}
