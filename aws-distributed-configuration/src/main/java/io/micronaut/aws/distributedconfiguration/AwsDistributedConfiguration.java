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

import io.micronaut.core.annotation.NonNull;

/**
 * Configuration for Distributed Configuration using AWS services such as AWS Parameter Store or Secrets Manager.
 *
 * @author Sergio del Amo
 * @since 2.7.0
 */
public interface AwsDistributedConfiguration {

    /**
     *
     * @return Prefix for AWS Distributed Configuration resources names.
     */
    @NonNull
    String getPrefix();

    /**
     *
     * @return Delimiter for AWS Distributed Configuration resources names.
     */
    @NonNull
    String getDelimiter();

    /**
     *
     * @return Leading Delimiter for AWS Distributed Configuration resources names.
     */
    @NonNull
    String getLeadingDelimiter();

    /**
     *
     * @return Trailing Delimiter for AWS Distributed Configuration resources names.
     */
    @NonNull
    String getTrailingDelimiter();

    /**
     *
     * @return Default shared configuration name.
     */
    @NonNull
    String getSharedConfigurationName();

    /**
     *
     * @return Weather paths with active environment names should be searched or not.
     */
    boolean isSearchActiveEnvironments();
}
