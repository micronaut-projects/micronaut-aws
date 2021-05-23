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
package io.micronaut.discovery.aws.parameterstore;

import io.micronaut.aws.distributedconfiguration.AwsDistributedConfiguration;
import io.micronaut.aws.distributedconfiguration.AwsDistributedConfigurationClient;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.runtime.ApplicationConfiguration;
import javax.inject.Singleton;

/**
 * Distributed configuration client for AWS System Manager Parameter Store.
 * @see <a href="https://docs.aws.amazon.com/systems-manager/latest/userguide/systems-manager-parameter-store.html">AWS System Manager Parameter Store</a>
 * @author Sergio del Amo
 * @since 2.7.0
 */
@Singleton
@BootstrapContextCompatible
public class AwsParameterStoreConfigurationClient extends AwsDistributedConfigurationClient {
    /**
     * @param awsDistributedConfiguration AWS Distributed Configuration
     * @param keyValueFetcher             a Key Value Fetcher
     * @param applicationConfiguration    Application Configuration
     */
    public AwsParameterStoreConfigurationClient(AwsDistributedConfiguration awsDistributedConfiguration,
                                                AwsParameterStoreKeyValuesFetcher keyValueFetcher,
                                                @Nullable ApplicationConfiguration applicationConfiguration) {
        super(awsDistributedConfiguration, keyValueFetcher, applicationConfiguration);
    }

    @Override
    @NonNull
    protected String getPropertySourceName() {
        return "awsparameterstore";
    }

    @Override
    public String getDescription() {
        return "AWS Parameter Store";
    }
}
