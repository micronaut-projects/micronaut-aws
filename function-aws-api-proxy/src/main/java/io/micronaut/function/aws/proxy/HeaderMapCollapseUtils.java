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
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpHeaders;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Utility methods for collapsing HTTP Headers.
 */
@Internal
public class HeaderMapCollapseUtils {
    private static final List<String> HEADERS_ALLOWING_COMMAS = Arrays.asList(HttpHeaders.DATE, HttpHeaders.USER_AGENT);
    private HeaderMapCollapseUtils() {

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
        return MapCollapseUtils.collapse(multi, single, MapCollapseUtils.COMMA, HEADERS_ALLOWING_COMMAS);
    }
}
