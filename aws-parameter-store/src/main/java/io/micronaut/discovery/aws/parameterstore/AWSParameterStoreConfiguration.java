/*
 * Copyright 2017-2020 original authors
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

import io.micronaut.aws.sdk.v1.AWSClientConfiguration;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.env.Environment;
import io.micronaut.core.util.StringUtils;
import io.micronaut.core.util.Toggleable;

import io.micronaut.core.annotation.NonNull;

/**
 * This is the configuration class for the AWSParameterStoreConfigClient for AWS Parameter Store based configuration.
 */
@Requires(env = Environment.AMAZON_EC2)
@Requires(property = AWSParameterStoreConfiguration.ENABLED, value = StringUtils.TRUE, defaultValue = StringUtils.FALSE)
@ConfigurationProperties(AWSParameterStoreConfiguration.CONFIGURATION_PREFIX)
@BootstrapContextCompatible
public class AWSParameterStoreConfiguration extends AWSClientConfiguration implements Toggleable  {

    /**
     * Constant for whether AWS parameter store is enabled or not.
     */
    public static final String ENABLED = "aws.client.system-manager.parameterstore.enabled";

    /**
     * The prefix for configuration.
     */
    public static final String CONFIGURATION_PREFIX = "system-manager.parameterstore";

    private static final String PREFIX = "config";
    private static final String DEFAULT_PATH = "/" + PREFIX + "/";
    private static final boolean DEFAULT_SECURE = false;
    private static final boolean DEFAULT_ENABLED = false;
    private static final boolean DEFAULT_SEARCH_ACTIVE_ENVIRONMENTS = true;

    private boolean useSecureParameters = DEFAULT_SECURE;
    private String rootHierarchyPath = DEFAULT_PATH;
    private boolean enabled = DEFAULT_ENABLED;
    private boolean searchActiveEnvironments = DEFAULT_SEARCH_ACTIVE_ENVIRONMENTS;

    /**
     * Enable or disable this feature.
     * @return enable or disable this feature.
     */
    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Enable or disable distributed configuration with AWS Parameter Store. Default value ({@value #DEFAULT_ENABLED}).
     *
     * @param enabled enable this feature
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * This is the default for the root hierarchy on the parameter store.
     *
     * @return root level of parameter hierarchy
     */
    @NonNull
    public String getRootHierarchyPath() {
        return rootHierarchyPath;
    }

    /**
     * The the root hierarchy on the parameter store. Default value ({@value #DEFAULT_PATH}).
     *
     * @param rootHierarchyPath root prefix used for all calls to get Parameter store values
     */
    public void setRootHierarchyPath(@NonNull String rootHierarchyPath) {
        this.rootHierarchyPath = rootHierarchyPath;
    }

    /**
     * @return Use auto encryption on SecureString types
     */
    public boolean getUseSecureParameters() {
        return useSecureParameters;
    }

    /**
     * Use auto-decryption via MKS for SecureString parameters. Default value ({@value #DEFAULT_SECURE}).
     * If set to false, you will not get unencrypted values.
     *
     * @param useSecureParameters True if secure parameters should be used
     */
    public void setUseSecureParameters(boolean useSecureParameters) {
        this.useSecureParameters = useSecureParameters;
    }

    /**
     * @return Search active environment paths
     */
    public boolean isSearchActiveEnvironments() {
        return searchActiveEnvironments;
    }

    /**
     * Search additional paths suffixed with each active environment.
     * e.g. /config/application_EC2
     * Default value ({@value #DEFAULT_SEARCH_ACTIVE_ENVIRONMENTS}).
     *
     * @param searchActiveEnvironments True if paths suffixed with micronaut profiles should be searched
     */
    public void setSearchActiveEnvironments(boolean searchActiveEnvironments) {
        this.searchActiveEnvironments = searchActiveEnvironments;
    }
}
