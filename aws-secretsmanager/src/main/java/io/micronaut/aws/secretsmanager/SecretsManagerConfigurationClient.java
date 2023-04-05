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

import io.micronaut.aws.distributedconfiguration.AwsDistributedConfiguration;
import io.micronaut.aws.distributedconfiguration.AwsDistributedConfigurationClient;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.runtime.ApplicationConfiguration;
import jakarta.inject.Singleton;

import java.util.Optional;

/**
 * Distributed configuration client for AWS Secrets Manager.
 * @see <a href="https://aws.amazon.com/secrets-manager/">AWS Secrets Manager</a>
 * @author Sergio del Amo
 * @since 2.8.0
 */
@Requires(beans = {
        AwsDistributedConfiguration.class,
        SecretsManagerGroupNameAwareKeyValueFetcher.class
})
@Singleton
@BootstrapContextCompatible
public class SecretsManagerConfigurationClient extends AwsDistributedConfigurationClient {

    private final Optional<SecretsManagerConfiguration> secretsManagerConfiguration;

    /**
     * @param awsDistributedConfiguration AWS Distributed Configuration
     * @param secretsManagerKeyValueFetcher Secrets Manager Key Value Fetcher
     * @param applicationConfiguration Application Configuration
     * @param secretsManagerConfiguration Secrets Configuration
     */
    public SecretsManagerConfigurationClient(AwsDistributedConfiguration awsDistributedConfiguration,
                                             SecretsManagerKeyValueFetcher secretsManagerKeyValueFetcher,
                                             @Nullable ApplicationConfiguration applicationConfiguration,
                                             SecretsManagerConfiguration secretsManagerConfiguration) {
        super(awsDistributedConfiguration, secretsManagerKeyValueFetcher, applicationConfiguration);
        this.secretsManagerConfiguration = Optional.of(secretsManagerConfiguration);
    }

    @Override
    @NonNull
    protected String adaptPropertyKey(String originalKey, String groupName) {
        if (secretsManagerConfiguration.isPresent()) {
            SecretsManagerConfiguration secretsConfiguration = secretsManagerConfiguration.get();
            if (CollectionUtils.isNotEmpty(secretsConfiguration.getSecrets())) {
                for (SecretsManagerConfigurationProperties.SecretConfiguration secret : secretsConfiguration.getSecrets()) {
                    if (groupName.endsWith(secret.getSecretName())) {
                        return secret.getPrefix() + "." + originalKey;
                    }
                }
            }
        }
        return originalKey;
    }

    @Override
    @NonNull
    protected String getPropertySourceName() {
        return "awssecretsmanager";
    }

    @Override
    @NonNull
    public String getDescription() {
        return "AWS Secrets Manager";
    }

}
