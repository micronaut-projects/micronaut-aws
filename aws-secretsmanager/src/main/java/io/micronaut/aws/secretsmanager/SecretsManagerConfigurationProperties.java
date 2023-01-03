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

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.core.annotation.Introspected;

import java.util.List;

/**
 * {@link ConfigurationProperties} implementation of {@link SecretsManagerConfiguration}.
 * @author Sergio del Amo
 * @since 2.8.0
 */
@BootstrapContextCompatible
@ConfigurationProperties(SecretsManagerConfigurationProperties.PREFIX)
public class SecretsManagerConfigurationProperties implements SecretsManagerConfiguration {

    /**
     * Prefix for Amazon EC2 configuration metadata.
     */
    public static final String PREFIX = AWSConfiguration.PREFIX + ".secretsmanager";

    /**
     * The default enable value.
     */
    @SuppressWarnings("WeakerAccess")
    public static final boolean DEFAULT_ENABLED = true;

    protected List<SecretConfiguration> secrets;

    private boolean enabled = DEFAULT_ENABLED;

    /**
     * @return Whether the AWS Secrets Manager configuration is enabled
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Default value ({@value #DEFAULT_ENABLED}).
     * @param enabled Enable or disable the AWS Secrets Manager configuration.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    @Override
    public List<SecretConfiguration> getSecrets() {
        return secrets;
    }

    /**
     * Secret configuration holder that allows for flexibility in secret key naming in the Micronaut context to avoid a potential keys name collision.
     * This is provided by an option to define a key group prefix for any secret name.
     *
     * @author sbodvanski
     * @since 3.8.0
     */
    @Introspected
    @EachProperty(value = "secrets", list = true)
    @BootstrapContextCompatible
    public static class SecretConfiguration {
        private String secretName;
        private String prefix;

        /**
         * Sets secret name.
         *
         * @param secretName secret name
         */
        public void setSecretName(String secretName) {
            this.secretName = secretName;
        }

        /**
         * Sets the group key prefix.
         *
         * @param prefix prefix
         */
        public void setPrefix(String prefix) {
            this.prefix = prefix;
        }

        /**
         * @return a secret name
         */
        public String getSecretName() {
            return secretName;
        }

        /**
         * @return a secret key group prefix
         */
        public String getPrefix() {
            return prefix;
        }
    }
}
