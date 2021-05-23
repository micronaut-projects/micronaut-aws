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

import io.micronaut.aws.AWSConfiguration;
import io.micronaut.context.annotation.BootstrapContextCompatible;
import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * This is the configuration class for the AWSParameterStoreConfigClient for AWS Parameter Store based configuration.
 */
@ConfigurationProperties(AWSParameterStoreConfigurationProperties.PREFIX)
@BootstrapContextCompatible
public class AWSParameterStoreConfigurationProperties implements AWSParameterStoreConfiguration  {

    /**
     * The prefix for configuration.
     */
    public static final String PREFIX = AWSConfiguration.PREFIX + ".parameterstore";

    /**
     * The default secure value.
     */
    @SuppressWarnings("WeakerAccess")
    public static final boolean DEFAULT_SECURE = false;

    /**
     * The default enable value.
     */
    @SuppressWarnings("WeakerAccess")
    public static final boolean DEFAULT_ENABLED = true;

    private boolean useSecureParameters = DEFAULT_SECURE;
    private boolean enabled = DEFAULT_ENABLED;

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

    @Override
    public boolean getUseSecureParameters() {
        return useSecureParameters;
    }

    /**
     * Use auto-decryption via KMS for SecureString parameters. Default value ({@value #DEFAULT_SECURE}).
     * If set to false, you will not get encrypted values.
     *
     * @param useSecureParameters True if secure parameters should be used
     */
    public void setUseSecureParameters(boolean useSecureParameters) {
        this.useSecureParameters = useSecureParameters;
    }
}
