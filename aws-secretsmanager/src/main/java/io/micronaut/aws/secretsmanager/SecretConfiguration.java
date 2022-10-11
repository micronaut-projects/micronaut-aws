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
package io.micronaut.aws.secretsmanager;

import io.micronaut.core.annotation.Introspected;

/**
 * Secret configuration holder that allows for flexibility in secret key naming in the Micronaut context to avoid a potential keys name collision.
 * This is provided by an option to define a key group prefix for any secret name.
 *
 * @author sbodvanski
 * @since 3.8.0
 */
@Introspected
public final class SecretConfiguration {
    private final String secretName;
    private final String prefix;

    public SecretConfiguration(String secret, String prefix) {
        this.secretName = secret;
        this.prefix = prefix;
    }

    /**
     * @return A secret name
     */
    public String getSecretName() {
        return secretName;
    }

    /**
     * @return A secret key group prefix
     */
    public String getPrefix() {
        return prefix;
    }
}
