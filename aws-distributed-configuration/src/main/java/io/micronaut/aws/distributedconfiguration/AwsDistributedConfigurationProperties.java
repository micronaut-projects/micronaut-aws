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
import io.micronaut.core.annotation.Experimental;
import io.micronaut.core.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

/**
 * {@link ConfigurationProperties} implementation of {@link AwsDistributedConfiguration}.
 *
 * @author Sergio del Amo
 * @since 2.8.0
 */
@BootstrapContextCompatible
@ConfigurationProperties(AwsDistributedConfigurationProperties.PREFIX)
@Experimental
public class AwsDistributedConfigurationProperties implements AwsDistributedConfiguration {

    public static final String PREFIX = AWSConfiguration.PREFIX + ".distributed-configuration";

    public static final String DEFAULT_PREFIX = "/config/";

    public static final String DEFAULT_COMMON_APPLICATION_NAME = "application";

    public static final String DEFAULT_DELIMETER = "/";

    public static final boolean DEFAULT_SEARCH_COMMON_APPLICATION = true;

    public static final boolean DEFAULT_SEARCH_ACTIVE_ENVIRONMENTS = true;

    private boolean searchActiveEnvironments = DEFAULT_SEARCH_ACTIVE_ENVIRONMENTS;

    private boolean searchCommonApplication = DEFAULT_SEARCH_COMMON_APPLICATION;

    @NonNull
    String prefix = DEFAULT_PREFIX;

    @NonNull
    private List<String> prefixes = new ArrayList<>();

    @NonNull
    private String commonApplicationName = DEFAULT_COMMON_APPLICATION_NAME;

    @NonNull
    private String delimiter = DEFAULT_DELIMETER;

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

    /**
     *
     * @return Whether paths for the {@link AwsDistributedConfiguration#getCommonApplicationName()} should be searched or not.
     */
    @Override
    public boolean isSearchCommonApplication() {
        return searchCommonApplication;
    }

    /**
     * Whether paths for the {@link AwsDistributedConfiguration#getCommonApplicationName()} should be searched or not. Default value ({@value #DEFAULT_SEARCH_COMMON_APPLICATION}).
     * @param searchCommonApplication Whether paths for the {@link AwsDistributedConfiguration#getCommonApplicationName()} should be searched or not.
     */
    public void setSearchCommonApplication(boolean searchCommonApplication) {
        this.searchCommonApplication = searchCommonApplication;
    }

    @Override
    @NonNull
    public List<String> getPrefixes() {
        return prefixes;
    }

    public void setPrefixes(@NonNull List<String> prefixes) {
        this.prefixes = prefixes;
    }

    @Override
    @NonNull
    public String getPrefix() {
        return prefix;
    }

    /**
     * Prefix for AWS Distributed Configuration resources names. Default ({@value #DEFAULT_PREFIX})
     * @param prefix Prefix for AWS Distributed Configuration resources names. For example `/config/`
     */
    public void setPrefix(@NonNull String prefix) {
        this.prefix = prefix;
    }

    @Override
    @NonNull
    public String getCommonApplicationName() {
        return commonApplicationName;
    }

    /**
     * Default Application name. Default value ({@value #DEFAULT_COMMON_APPLICATION_NAME}.
     * @param commonApplicationName Default Application name. e.g. application
     */
    public void setCommonApplicationName(@NonNull String commonApplicationName) {
        this.commonApplicationName = commonApplicationName;
    }
}
