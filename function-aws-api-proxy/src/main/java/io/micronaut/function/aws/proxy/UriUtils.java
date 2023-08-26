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
import io.micronaut.core.util.StringUtils;

import java.net.URI;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Utility methods for URL parsing and generation.
 */
@Internal
public final class UriUtils {
    private static final String QUERY_PARAM_SEPARATOR = "&";
    private static final String QUERY_SEPARATOR = "?";

    private UriUtils() {
    }

    /**
     * Generates an URI from a Path and a Map of Query String Parameters.
     *
     * @param path The URL Path
     * @param queryStringParams The Query String parameters as a Multi Value Map
     * @return The URI
     */
    public static URI toURI(String path,
                            Map<String, List<String>> queryStringParams) {
       return toURI(path, Map.of(), queryStringParams);
    }

    /**
     * Generates an URI from a Path and a Map of Query String Parameters and a
     * Multi Value Map of Query String parameters.
     *
     * @param path The URL Path
     * @param queryStringParameters The simple query string parameters
     * @param multiValueQueryStringParameters The multi value Query String parameters
     * @return The URI
     */
    public static URI toURI(String path,
                            Map<String, String> queryStringParameters,
                            Map<String, List<String>> multiValueQueryStringParameters) {
        String queryPart = Stream.concat(
                Optional.ofNullable(queryStringParameters).orElse(Map.of())
                    .entrySet().stream()
                    .map(entry -> Map.entry(entry.getKey(), List.of(entry.getValue()))),
                Optional.ofNullable(multiValueQueryStringParameters).orElse(Map.of())
                    .entrySet().stream()
            )
            .map(entry -> entry.getValue().stream()
                .map(value -> entry.getKey() + "=" + value)
                .collect(Collectors.joining(QUERY_PARAM_SEPARATOR)))
            .collect(Collectors.joining(QUERY_PARAM_SEPARATOR));

        return toURI(path, queryPart);
    }

    /**
     * Generates an URI from a Path and a String representing query params.
     *
     * @param path The URL Path
     * @param rawQueryString The Query String parameters as a Multi Value Map
     * @return The URI
     */
    public static URI toURI(String path,
                            String rawQueryString) {
        String queryPart = Optional.ofNullable(rawQueryString)
            .filter(StringUtils::isNotEmpty)
            .map(query -> QUERY_SEPARATOR + query)
            .orElse("");
        return URI.create(path + queryPart);
    }

}
