/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.aws.distributedconfiguration;

import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.NonNull;

import java.util.Map;
import java.util.Optional;

/**
 * Fetches a Map of property group names paired with a map of properties for a given prefix.
 *
 * @author sbodvanski
 * @since 3.8.0
 */
@Experimental
@FunctionalInterface
public interface GroupNameAwareKeyValueFetcher {
    /**
     *
     * @param prefix AWS Distributed Configuration Resource's name prefix. E.g. /config/application_dev/
     * @return A Map of configuration properties
     */
    @NonNull
    Optional<Map<String, Map<String, ?>>> keyValuesByPrefix(@NonNull String prefix);
}
