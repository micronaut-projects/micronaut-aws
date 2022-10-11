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
import io.micronaut.core.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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

    private boolean enabled = DEFAULT_ENABLED;

    private List<Map<String, String>> secrets;

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

    /**
     * Sets the secret configuration.
     *
     * @param secrets the secret configuration
     */
    public void setSecrets(List<Map<String, String>> secrets) {
        this.secrets = secrets;
    }

    /**
     * @return the secret configuration
     */
    @Override
    public List<SecretConfiguration> getSecrets() {
        List<SecretConfiguration> secretHolders = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(secrets)) {
            for (Map<String, String> secretHolderMap : secrets) {
                Optional<SecretConfiguration> secretHolder = convertMapToSecretHolder(secretHolderMap);
                if (secretHolder.isPresent()) {
                    secretHolders.add(secretHolder.get());
                }
            }
        }
        return secretHolders;
    }

    private Optional<SecretConfiguration> convertMapToSecretHolder(Map<String, String> secretHolderMap) {
        if (CollectionUtils.isNotEmpty(secretHolderMap)) {
            SecretConfiguration secretHolder = new SecretConfiguration(secretHolderMap.get("secretName"), secretHolderMap.get("prefix"));
            return Optional.of(secretHolder);
        }
        return Optional.empty();
    }
}
