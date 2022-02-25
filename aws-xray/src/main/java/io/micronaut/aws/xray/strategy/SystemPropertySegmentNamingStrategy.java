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

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;
import java.util.Optional;

/**
 * If the system property {@value SYSTEM_PROPERTY_KEY_TRACING_NAME} is present, its value is used as the segment name.
 * @author Sergio del Amo
 * @since 3.2.0
 * @param <T> Request
 */
@Requires(condition = SystemPropertySegmentNamingStrategyCondition.class)
@Singleton
public class SystemPropertySegmentNamingStrategy<T> implements SegmentNamingStrategy<T> {
    /**
     * System property key used to override the default segment name.
     */
    public static final String SYSTEM_PROPERTY_KEY_TRACING_NAME = "com.amazonaws.xray.strategy.tracingName";

    public static final int ORDER = EnvironmentVariableSegmentNamingStrategy.ORDER + 100;

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    @NonNull
    public Optional<String> resolveName(@NonNull T request) {
        return Optional.ofNullable(System.getProperty(SYSTEM_PROPERTY_KEY_TRACING_NAME));
    }
}
