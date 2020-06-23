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
package io.micronaut.aws.alexa.httpserver.conf;

import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.aws.alexa.conf.AlexaSkillConfigurationProperties;
import io.micronaut.context.annotation.ConfigurationProperties;

/**
 * {@link ConfigurationProperties} implementation of {@link AlexaControllerConfiguration}.
 */
@ConfigurationProperties(AlexaControllerConfigurationProperties.PREFIX)
public class AlexaControllerConfigurationProperties implements AlexaControllerConfiguration {

    public static final String PREFIX = AlexaSkillConfigurationProperties.PREFIX + ".endpoint";

    public static final String DEFAULT_PATH = "/alexa";

    private static final boolean DEFAULT_ENABLED = true;

    private boolean enabled = DEFAULT_ENABLED;

    @Nullable
    private String path = DEFAULT_PATH;

    @Override
    @Nullable
    public String getPath() {
        return path;
    }

    /**
     * Default value ({@value #DEFAULT_PATH}).
     * @param path The path to alexa endpoint.
     */
    public void setPath(@Nullable String path) {
        this.path = path;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Sets whether this configuration is enabled. Default {@value #DEFAULT_ENABLED}.
     *
     * @param enabled The enabled setting
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
