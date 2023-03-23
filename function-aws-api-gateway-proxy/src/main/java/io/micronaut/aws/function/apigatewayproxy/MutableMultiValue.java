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

import io.micronaut.core.convert.ConversionService;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class MutableMultiValue extends MultiValue {
    public MutableMultiValue(ConversionService conversionService, Map<String, List<String>> multi, Map<String, String> single) {
        super(conversionService, multi, single);
    }

    public void add(CharSequence name, List<CharSequence> valuesToBeAdded) {
        String key = HttpHeaderUtils.normalizeHttpHeaderCase(name.toString());
        values.computeIfAbsent(key, s -> new ArrayList<>());
        values.get(key).addAll(valuesToBeAdded.stream().map(CharSequence::toString).toList());
    }

    public void add(CharSequence name, CharSequence value) {
        String key = HttpHeaderUtils.normalizeHttpHeaderCase(name.toString());
        values.computeIfAbsent(key, s -> new ArrayList<>());
        values.get(key).add(value.toString());
    }

    public void remove(CharSequence header) {
        values.remove(header.toString());
    }

    public void setConversionService(ConversionService conversionService) {
        this.conversionService = conversionService;
    }
}
