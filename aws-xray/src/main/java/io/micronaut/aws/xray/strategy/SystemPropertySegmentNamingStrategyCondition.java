/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.aws.xray.strategy;

import io.micronaut.context.condition.Condition;
import io.micronaut.context.condition.ConditionContext;

/**
 * Evaluates to true if the system property {@link SystemPropertySegmentNamingStrategy#SYSTEM_PROPERTY_KEY_TRACING_NAME} is present.
 * @author Sergio del Amo
 * @since 2.7.0
 */
public class SystemPropertySegmentNamingStrategyCondition implements Condition {
    @Override
    public boolean matches(ConditionContext context) {
        return System.getenv(EnvironmentVariableSegmentNamingStrategy.ENVIRONMENT_VARIABLE_AWS_XRAY_TRACING_NAME) != null;
    }
}
