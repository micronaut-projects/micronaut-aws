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

import com.amazon.ask.Skills;
import com.amazon.ask.builder.SkillBuilder;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.context.annotation.Requires;
import io.micronaut.context.annotation.Secondary;

import jakarta.inject.Singleton;

/**
 * Creates a builder used to construct a new {@link com.amazon.ask.Skill} using the default
 * {@link com.amazon.ask.attributes.persistence.impl.DynamoDbPersistenceAdapter}
 * and {@link com.amazon.ask.services.ApacheHttpApiClient}.
 *
 * @author sdelamo
 * @since 2.0.0
 */
@Secondary
@Requires(classes = Skills.class)
@Singleton
public class StandardSkillBuilderProvider implements SkillBuilderProvider {

    @Override
    @NonNull
    public SkillBuilder<?> getSkillBuilder() {
        return Skills.standard();
    }
}
