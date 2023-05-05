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

import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.MutableHttpHeaders;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class MapCollapseUtils {

    private MapCollapseUtils() {

    }
    public static Map<String, String> getSingleValueHeaders(MutableHttpHeaders headers) {
        Map<String, String> result = new HashMap<>();
        for (String paramName : headers.names()) {
            result.put(paramName, headers.get(paramName));
        }
        return result;
    }

    public static Map<String, List<String>> getMulitHeaders(MutableHttpHeaders headers) {
        Map<String, List<String>> result = new HashMap<>();
        for (String paramName : headers.names()) {
            result.put(paramName, headers.getAll(paramName));
        }
        return result;
    }


    public static Map<String, List<String>> collapse(Map<String, List<String>> multi, Map<String, String> single) {
        if (multi == null && single == null) {
            return Collections.emptyMap();
        } else {
            Map<String, List<String>> values = new HashMap<>();
            if (multi != null) {
                for (String name : multi.keySet()) {
                    values.computeIfAbsent(name, s -> new ArrayList<>());
                    values.get(name).addAll(multi.get(name));
                }
            }
            if (CollectionUtils.isNotEmpty(single)) {
                for (String name : single.keySet()) {
                    values.computeIfAbsent(name, s -> new ArrayList<>());
                    values.get(name).add(single.get(name));
                }
            }
            return values;
        }
    }
}
