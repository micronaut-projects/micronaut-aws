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

import io.micronaut.core.annotation.Internal;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.Properties;

/**
 * @author Sergio del Amo
 * @since 3.10.0
 */
@Internal
public class VersionInfo {

    private static final Properties VERSIONS = new Properties();

    static {
        URL resource = VersionInfo.class.getResource("/micronaut-versions.properties");
        if (resource != null) {
            try (Reader reader = new InputStreamReader(resource.openStream(), StandardCharsets.UTF_8)) {
                VERSIONS.load(reader);
            } catch (IOException e) {
                // ignore
            }
        }
    }

    public static Optional<String> getMicronautVersion() {
        Object micronautVersion = VERSIONS.get("micronaut.version");
        return Optional.ofNullable(micronautVersion).map(Object::toString);
    }
}
