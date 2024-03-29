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
package io.micronaut.aws.alexa.locale;

import com.amazon.ask.dispatcher.request.handler.HandlerInput;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.order.Ordered;
import io.micronaut.core.util.locale.FixedLocaleResolver;
import jakarta.inject.Singleton;

/**
 * Generic implementation of {@link io.micronaut.core.util.LocaleResolver} for fixed locale resolution.
 *
 * @author Sergio del Amo
 * @since 3.10.0
 */
@Singleton
@Requires(property = HandlerInputLocaleResolutionConfigurationProperties.PREFIX + ".fixed")
public class HandlerInputFixedLocaleResolver extends FixedLocaleResolver<HandlerInput> implements HandlerInputLocaleResolver {

    public static final Integer ORDER = Ordered.HIGHEST_PRECEDENCE + 100;

    /**
     * @param localeResolutionConfiguration The locale resolution configuration
     */
    public HandlerInputFixedLocaleResolver(HandlerInputLocaleResolutionConfiguration localeResolutionConfiguration) {
        super(localeResolutionConfiguration.getFixed().orElseThrow(() -> new IllegalArgumentException("The fixed locale must be set")));
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
