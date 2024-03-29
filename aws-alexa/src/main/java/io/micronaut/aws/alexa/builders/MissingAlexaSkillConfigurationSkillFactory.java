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
package io.micronaut.aws.alexa.builders;

import com.amazon.ask.AlexaSkill;
import com.amazon.ask.Skill;
import io.micronaut.aws.alexa.conf.AlexaSkillConfiguration;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import jakarta.inject.Singleton;

/**
 * Creates an Alexa Skill only if no AlexaSkillConfiguration is present.
 *
 * @author sdelamo
 * @since 2.0.0
 */
@Factory
@Requires(beans = SkillBuilderProvider.class)
@Requires(beans = AlexaSkillBuilder.class)
@Requires(missingBeans = AlexaSkillConfiguration.class)
public class MissingAlexaSkillConfigurationSkillFactory {
    private final AlexaSkillBuilder alexaSkillBuilder;
    private final SkillBuilderProvider skillBuilderProvider;

    /**
     *
     * @param alexaSkillBuilder Alexa Skill Builder
     * @param skillBuilderProvider Skill Builder Provider
     */
    public MissingAlexaSkillConfigurationSkillFactory(AlexaSkillBuilder alexaSkillBuilder,
                                SkillBuilderProvider skillBuilderProvider) {
        this.alexaSkillBuilder = alexaSkillBuilder;
        this.skillBuilderProvider = skillBuilderProvider;
    }

    /**
     *
     * @return An Alexa Skill using the {@link AlexaSkillBuilder} and the {@link SkillBuilderProvider} bean.
     */
    @Singleton
    public AlexaSkill createStandardAlexaSkill() {
        return alexaSkillBuilder.buildSkill(skillBuilderProvider.getSkillBuilder(), null);
    }

    /**
     *
     * @return An Skill using the {@link AlexaSkillBuilder} and the {@link SkillBuilderProvider} bean.
     */
    @Singleton
    public Skill createSkill() {
        AlexaSkill alexaSkill = alexaSkillBuilder.buildSkill(skillBuilderProvider.getSkillBuilder(), null);
        if (alexaSkill instanceof Skill) {
            return (Skill) alexaSkill;
        }
        return null;
    }
}
