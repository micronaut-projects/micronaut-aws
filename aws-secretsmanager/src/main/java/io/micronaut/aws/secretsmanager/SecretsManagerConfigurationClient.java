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

import io.micronaut.aws.distributedconfiguration.AwsDistributedConfigurationClient;
import io.micronaut.aws.distributedconfiguration.AwsDistributedConfigurationProperties;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import jakarta.inject.Singleton;

/**
 * @author Sergio del Amo
 * @author Matej Nedic
 * @since ?
 */
@Requires(beans = {
        SecretsManagerKeyValueFetcher.class,
        AwsDistributedConfigurationProperties.class
})
@Singleton
@BootstrapContextCompatible
public class SecretsManagerConfigurationClient extends AwsDistributedConfigurationClient {

    public SecretsManagerConfigurationClient(SecretsManagerKeyValueFetcher secretsManagerKeyValueFetcher,
                                             @Nullable AwsDistributedConfigurationProperties awsDistributedConfigurationProperties) {
        super(secretsManagerKeyValueFetcher, awsDistributedConfigurationProperties);
    }

    @Override
    @NonNull
    protected String getPropertySourceName() {
        return "awssecretsmanager";
    }

    @Override
    public String getDescription() {
        return "AWS Secrets Manager";
    }
}
