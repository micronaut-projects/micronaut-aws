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
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.MutableHttpHeaders;

import java.util.Arrays;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for collapsing headers.
 */
@Internal
public final class MapCollapseUtils {

    /**
     * Comma.
     */
    public static final String COMMA = ",";

    private MapCollapseUtils() {
    }

    /**
     * Collapse the headers into a single value map.
     *
     * @param headers The headers
     * @return The map
     */
    public static Map<String, String> getSingleValueHeaders(MutableHttpHeaders headers) {
        Map<String, String> result = new HashMap<>();
        for (String paramName : headers.names()) {
            result.put(paramName, String.join(COMMA, headers.getAll(paramName)));
        }
        return result;
    }

    /**
     * Collapse the headers into a multi value map.
     *
     * @param headers The headers
     * @return The map
     */
    public static Map<String, List<String>> getMultiHeaders(MutableHttpHeaders headers) {
        Map<String, List<String>> result = new HashMap<>();
        for (String paramName : headers.names()) {
            result.put(paramName, headers.getAll(paramName));
        }
        return result;
    }

    /**
     * Collapse the aws single and multi headers into a single value map.
     *
     * @param multi  The multi value map
     * @param single The single value map
     * @return The map
     */
    public static Map<String, List<String>> collapse(@Nullable Map<String, List<String>> multi,
                                                      @Nullable Map<String, String> single) {
        return collapse(multi, single, null, null);
    }

    /**
     * Collapse the aws single and multi headers into a single value map.
     *
     * @param multi  The multi value map
     * @param single The single value map
     * @param splitRegex Regular expression to split the map value with
     * @param dontSplitKeys Collection of keys which should not be split
     * @return The map
     */

    public static Map<String, List<String>> collapse(@Nullable Map<String, List<String>> multi,
                                                     @Nullable Map<String, String> single,
                                                     @Nullable String splitRegex,
                                                     @Nullable List<String> dontSplitKeys) {
        if (multi == null && single == null) {
            return Collections.emptyMap();
        }
        Map<String, List<String>> values = new HashMap<>();
        if (multi != null) {
            for (var entry: multi.entrySet()) {
                values.computeIfAbsent(entry.getKey(), s -> new ArrayList<>()).addAll(entry.getValue());
            }
        }
        if (CollectionUtils.isNotEmpty(single)) {
            for (var entry: single.entrySet()) {
                String headerName = entry.getKey();
                List<String> headerValues = values.computeIfAbsent(headerName, s -> new ArrayList<>());
                if (dontSplitKeys != null && splitRegex != null) {
                    populateHeaderValues(headerName, entry.getValue(), splitRegex, dontSplitKeys, headerValues);
                } else {
                    String v = entry.getValue();
                    if (!headerValues.contains(v)) {
                        headerValues.add(v);
                    }
                }
            }
        }
        return values;
    }

    private static void populateHeaderValues(@NonNull String headerName,
                                             @NonNull String headerValue,
                                             @NonNull String regex,
                                             @NonNull List<String> dontSplitKeys,
                                             @NonNull List<String> headerValues) {
        if (dontSplitKeys.contains(headerName)) {
            if (!headerValues.contains(headerValue)) {
                headerValues.add(headerValue);
            }
        } else {
            for (String v : split(headerValue, regex)) {
                v = v.trim();
                if (!headerValues.contains(v)) {
                    headerValues.add(v);
                }
            }
        }
    }

    /**
     * Collapse a map whose value is a list of strings into a map whose value is a comma separated string.
     *
     * @param input Map with key String and value List of Strings
     * @return Map with key String and value String with comma separated values
     */
    public static Map<String, String> collapse(Map<String, List<String>> input) {
        Map<String, String> result = new HashMap<>();
        for (Map.Entry<String, List<String>> entry : input.entrySet()) {
            result.put(entry.getKey(), String.join(COMMA, entry.getValue()));
        }
        return result;
    }

    @NonNull
    private static List<String> split(@Nullable String value, @NonNull String regex) {
        if (value == null) {
            return Collections.emptyList();
        }
        return Arrays.asList(value.split(regex));
    }
}
