/*
 * Copyright 2017-2022 original authors
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
package io.micronaut.aws.ua;

import io.micronaut.core.annotation.NonNull;

import static io.micronaut.aws.ua.VersionInfo.getMicronautVersion;

/**
 * Utility class to provide a value for the {@code User-Agent} HTTP Header when communicating with AWS SDK.
 * @see <a href="https://developer.mozilla.org/en-US/docs/Web/HTTP/Headers/User-Agent">User-Agent</a>.
 * @author Sergio del Amo
 * @since 3.10.0
 */
public final class UserAgentUtils {
    private static final String PREFIX = "micronaut";

    private UserAgentUtils() {
    }

    /**
     *
     * @return User agent value. For example micronaut/3.7.1
     */
    @NonNull
    public static String userAgent() {
        return getMicronautVersion()
            .map(micronautVersion -> String.format("%s/%s", PREFIX, micronautVersion))
            .orElse(PREFIX);
    }
}
