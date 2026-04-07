/*
 * Copyright 2017-2026 original authors
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
package io.micronaut.aws.distributedconfiguration.imports;

import io.micronaut.core.annotation.Internal;
import org.slf4j.Logger;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Emits deprecation warnings once per backend.
 *
 * @since 5.0.0
 */
@Internal
public final class LegacyConfigClientDeprecationLogger {

    private static final Set<String> WARNED_KEYS = ConcurrentHashMap.newKeySet();

    /**
     * Utility class.
     */
    private LegacyConfigClientDeprecationLogger() {
    }

    /**
     * Log the warning only once for the given key.
     *
     * @param logger The logger
     * @param key The deduplication key
     * @param message The message
     */
    public static void warn(Logger logger, String key, String message) {
        if (WARNED_KEYS.add(key)) {
            logger.warn(message);
        }
    }
}
