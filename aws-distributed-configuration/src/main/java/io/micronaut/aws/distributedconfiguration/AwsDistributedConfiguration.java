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
package io.micronaut.aws.distributedconfiguration;

import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.NonNull;
import java.util.List;

/**
 * Configuration for Distributed Configuration using AWS services such as AWS Parameter Store or Secrets Manager.
 *
 * @author Sergio del Amo
 * @since 2.8.0
 */
@Experimental
public interface AwsDistributedConfiguration {

    /**
     *
     * @return Prefix for AWS Distributed Configuration resources names. For example `/config/`
     */
    @NonNull
    String getPrefix();

    /**
     * @return List of prefixes for AWS Distributed Configuration resources names. If it is non-empty,
     * {@link AwsDistributedConfiguration#getPrefix()} is not used
     */
    @NonNull
    List<String> getPrefixes();

    /**
     * Delimiter after prefix and application name. For /config/application_dev/micronaut.security.oauth2.clients.mycompanyauth.client-secret
     * delimiter will be / The character between  /config/application_dev and micronaut.security.oauth2.clients.mycompanyauth.client-secret
     * @return Delimiter after {@link AwsDistributedConfiguration#getPrefix} and application name for AWS Distributed Configuration resources names.
     */
    @NonNull
    String getDelimiter();

    /**
     *
     * @return Default Application name. e.g. application
     */
    @NonNull
    String getCommonApplicationName();

    /**
     *
     * @return Whether paths for the {@link AwsDistributedConfiguration#getCommonApplicationName()} should be searched or not.
     */
    boolean isSearchCommonApplication();

    /**
     *
     * @return Whether paths with active environment names should be searched or not.
     */
    boolean isSearchActiveEnvironments();
}
