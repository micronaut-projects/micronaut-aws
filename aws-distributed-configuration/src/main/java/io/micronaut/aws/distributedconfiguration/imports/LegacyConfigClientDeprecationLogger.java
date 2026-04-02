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
