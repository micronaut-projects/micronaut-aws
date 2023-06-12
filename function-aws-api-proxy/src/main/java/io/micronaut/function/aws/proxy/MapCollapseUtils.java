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
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.MutableHttpHeaders;

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
            result.put(paramName, headers.get(paramName));
        }
        return result;
    }

    /**
     * Collapse the headers into a multi value map.
     *
     * @param headers The headers
     * @return The map
     */
    public static Map<String, List<String>> getMulitHeaders(MutableHttpHeaders headers) {
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
    public static Map<String, List<String>> collapse(Map<String, List<String>> multi, Map<String, String> single) {
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
                List<String> strings = values.computeIfAbsent(entry.getKey(), s -> new ArrayList<>());
                if (!strings.contains(entry.getValue())) {
                    strings.add(entry.getValue());
                }
            }
        }
        return values;
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
            result.put(entry.getKey(), String.join(",", entry.getValue()));
        }
        return result;
    }
}
