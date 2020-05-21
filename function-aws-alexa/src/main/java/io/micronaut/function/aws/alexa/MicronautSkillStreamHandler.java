/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.function.aws.alexa;

import com.amazon.ask.AlexaSkill;
import com.amazon.ask.SkillStreamHandler;

/**
 * Implementation of {@link SkillStreamHandler}.
 *
 * @author sdelamo
 * @since 2.0.0
 */
public class MicronautSkillStreamHandler extends SkillStreamHandler {

    /**
     * Constructor to build an instance of {@link SkillStreamHandler} with a single Alexa skill.
     * @param skill instance of type {@link AlexaSkill}.
     */
    public MicronautSkillStreamHandler(final AlexaSkill skill) {
        super(skill);
    }

    /**
     * Constructor to build an instance of {@link SkillStreamHandler} with multiple Alexa skills.
     * @param skills instances of type {@link AlexaSkill}.
     */
    public MicronautSkillStreamHandler(final AlexaSkill... skills) {
        super(skills);
    }

}
