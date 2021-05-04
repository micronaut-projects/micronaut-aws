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
import com.amazon.ask.builder.SkillBuilder;
import io.micronaut.aws.alexa.conf.AlexaSkillConfiguration;
import io.micronaut.context.annotation.DefaultImplementation;

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import javax.validation.constraints.NotNull;

/**
 * {@link FunctionalInterface} to create {@link AlexaSkill}.
 * @param <Request> input type.
 * @param <Response> output type.
 * @author sdelamo
 * @since 2.0.0
 */
@FunctionalInterface
@DefaultImplementation(DefaultAlexaSkillBuilder.class)
public interface AlexaSkillBuilder<Request, Response> {
    /**
     *
     * @param skillBuilder A Skill builder
     * @param alexaSkillConfiguration Alexa Skill Configuration
     * @return An Alexa skill.
     */
    @NonNull
    AlexaSkill<Request, Response> buildSkill(@NotNull @NonNull SkillBuilder<?> skillBuilder,
                                             @Nullable AlexaSkillConfiguration alexaSkillConfiguration);
}
