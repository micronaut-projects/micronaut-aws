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
package io.micronaut.aws.alexa.httpserver.services;

import com.amazon.ask.Skill;
import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.ResponseEnvelope;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.core.order.OrderUtil;

import javax.inject.Singleton;
import javax.validation.constraints.NotNull;
import java.util.List;
import java.util.stream.Collectors;

/**
 * {@link io.micronaut.context.annotation.DefaultImplementation} of {@link RequestEnvelopeService}.
 */
@Singleton
public class DefaultRequestEnvelopeService implements RequestEnvelopeService {
    /**
     * Collection of {@link Skill}.
     */
    private final List<Skill> skills;

    /**
     *
     * @param skills List of available Skills
     */
    public DefaultRequestEnvelopeService(List<Skill> skills) {
        this.skills = skills.stream().sorted(OrderUtil.COMPARATOR).collect(Collectors.toList());
    }

    @Nullable
    @Override
    public ResponseEnvelope process(@NonNull @NotNull RequestEnvelope requestEnvelope) {
        for (Skill skill : skills) {
            ResponseEnvelope skillResponse = skill.invoke(requestEnvelope);
            if (skillResponse != null) {
                return skillResponse;
            }
        }
        return null;
    }
}
