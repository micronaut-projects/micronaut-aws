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

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.NonNull;

/**
 * {@link ConfigurationProperties} implementation of {@link AwsDistributedConfiguration}.
 *
 * @author Sergio del Amo
 * @since 2.7.0
 */
@BootstrapContextCompatible
@ConfigurationProperties(AwsDistributedConfigurationProperties.PREFIX)
public class AwsDistributedConfigurationProperties implements AwsDistributedConfiguration {

    public static final String PREFIX = AWSConfiguration.PREFIX + ".distributed-configuration";

    public static final String DEFAULT_PREFIX = "config";

    public static final String DEFAULT_SHARED_CONFIGURATION_NAME = "application";

    public static final String DEFAULT_DELIMETER = "/";

    public static final String DEFAULT_LEADING_DELIMETER = "/";

    public static final String DEFAULT_TRAILING_DELIMETER = "/";

    public static final boolean DEFAULT_SEARCH_ACTIVE_ENVIRONMENTS = true;

    private boolean searchActiveEnvironments = DEFAULT_SEARCH_ACTIVE_ENVIRONMENTS;

    @NonNull
    String prefix = DEFAULT_PREFIX;

    @NonNull
    private String sharedConfigurationName = DEFAULT_SHARED_CONFIGURATION_NAME;

    @NonNull
    private String delimiter = DEFAULT_DELIMETER;

    @NonNull
    private String leadingDelimiter = DEFAULT_LEADING_DELIMETER;

    @NonNull
    private String trailingDelimiter = DEFAULT_TRAILING_DELIMETER;

    @Override
    @NonNull
    public String getPrefix() {
        return prefix;
    }

    /**
     * Prefix for AWS Distributed Configuration resources names. Default value ({@value #DEFAULT_PREFIX}).
     * @param prefix Prefix for AWS Distributed Configuration resources names.
     */
    public void setPrefix(@NonNull String prefix) {
        this.prefix = prefix;
    }

    @Override
    @NonNull
    public String getSharedConfigurationName() {
        return sharedConfigurationName;
    }

    /**
     * Default shared configuration name. Default value ({@value #DEFAULT_SHARED_CONFIGURATION_NAME}).
     * @param sharedConfigurationName shared configuration name.
     */
    public void setSharedConfigurationName(@NonNull String sharedConfigurationName) {
        this.sharedConfigurationName = sharedConfigurationName;
    }

    @Override
    @NonNull
    public String getDelimiter() {
        return delimiter;
    }

    /**
     * Delimiter for AWS Distributed Configuration resources names. Default value ({@value #DEFAULT_DELIMETER}).
     * @param delimiter  Delimiter for AWS Distributed Configuration resources names.
     */
    public void setDelimiter(@NonNull String delimiter) {
        this.delimiter = delimiter;
    }

    @Override
    @NonNull
    public String getLeadingDelimiter() {
        return leadingDelimiter;
    }

    /**
     * Leading delimiter for AWS Distributed Configuration resources names. Default value ({@value #DEFAULT_LEADING_DELIMETER}).
     * @param leadingDelimiter  Leading Delimiter for AWS Distributed Configuration resources names.
     */
    public void setLeadingDelimiter(@NonNull String leadingDelimiter) {
        this.leadingDelimiter = leadingDelimiter;
    }

    @Override
    @NonNull
    public String getTrailingDelimiter() {
        return trailingDelimiter;
    }

    /**
     * Trailing delimiter for AWS Distributed Configuration resources names. Default value ({@value #DEFAULT_TRAILING_DELIMETER}).
     * @param trailingDelimiter  Trailing Delimiter for AWS Distributed Configuration resources names.
     */
    public void setTrailingDelimiter(@NonNull String trailingDelimiter) {
        this.trailingDelimiter = trailingDelimiter;
    }

    /**
     * @return Search active environment paths
     */
    @Override
    public boolean isSearchActiveEnvironments() {
        return searchActiveEnvironments;
    }

    /**
     * Search additional paths suffixed with each active environment.
     * e.g. /config/application_ec2
     * Default value ({@value #DEFAULT_SEARCH_ACTIVE_ENVIRONMENTS}).
     *
     * @param searchActiveEnvironments True if paths suffixed with micronaut environments should be searched
     */
    public void setSearchActiveEnvironments(boolean searchActiveEnvironments) {
        this.searchActiveEnvironments = searchActiveEnvironments;
    }
}
