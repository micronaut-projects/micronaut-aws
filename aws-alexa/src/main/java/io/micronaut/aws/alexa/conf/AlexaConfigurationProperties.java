/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.aws.alexa.conf;

import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Introspected;

import javax.validation.constraints.NotBlank;

import static io.micronaut.aws.alexa.conf.AlexaConfigurationProperties.PREFIX;

/**
 * This allows configuring properties that area AWS Alexa specific such as skill-id for skill verification.
 *
 * @author sdelamo
 * @since 2.0.0
 */
@Introspected
@ConfigurationProperties(PREFIX)
@Requires(property = PREFIX + ".skill-id")
@Requires(env = AlexaEnvironment.ENV_ALEXA)
public class AlexaConfigurationProperties implements AlexaConfiguration {

    static final String PREFIX = "alexa";

    @NotBlank
    @NonNull
    private String skillId;

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
