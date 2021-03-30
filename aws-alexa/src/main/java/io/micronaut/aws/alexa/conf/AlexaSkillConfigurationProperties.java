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
package io.micronaut.aws.alexa.conf;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.context.annotation.EachProperty;
import io.micronaut.context.annotation.Parameter;
import io.micronaut.core.annotation.Introspected;
import javax.validation.constraints.NotBlank;


/**
 * This allows configuring properties that area AWS Alexa specific such as skill-id for skill verification.
 *
 * @author sdelamo
 * @since 2.0.0
 */
@Introspected
@EachProperty(AlexaSkillConfigurationProperties.SKILLS_PREFIX)
public class AlexaSkillConfigurationProperties implements AlexaSkillConfiguration {

    public static final String PREFIX = "alexa";

    public static final String SKILLS_PREFIX = PREFIX + ".skills";

    private static final boolean DEFAULT_ENABLED = true;

    private boolean enabled = DEFAULT_ENABLED;

    @NotBlank
    @NonNull
    private String skillId;

    private final String name;

    /**
     * @param name The name of the configuration
     */
    public AlexaSkillConfigurationProperties(@Parameter String name) {
        this.name = name;
    }

    /**
     * @return The name of the configuration
     */
    @Override
    @NonNull
    public String getName() {
        return name;
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

    /**
     * The Skill ID of this Alexa skill.
     * @return skill id
     */
    @Override
    @NonNull
    public String getSkillId() {
        return skillId;
    }

    /**
     * The Skill ID of this Alexa skill.
     * @param skillId skill id
     */
    public void setSkillId(@NonNull String skillId) {
        this.skillId = skillId;
    }
}
