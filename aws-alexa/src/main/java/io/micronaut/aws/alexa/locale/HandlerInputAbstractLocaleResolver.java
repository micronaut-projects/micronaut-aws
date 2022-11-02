/*
 * Copyright 2017-2022 original authors
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
import io.micronaut.core.util.locale.AbstractLocaleResolver;

/**
 * Provides an abstract class which implements {@link io.micronaut.core.util.LocaleResolver} and handles default locale resolution.
 * @author Sergio del Amo
 * @since 3.10.0
 */
public abstract class HandlerInputAbstractLocaleResolver extends AbstractLocaleResolver<HandlerInput> implements HandlerInputLocaleResolver {
    public static final Integer ORDER = 50;

    protected final HandlerInputLocaleResolutionConfiguration localeResolutionConfiguration;

    /**
     * @param localeResolutionConfiguration The locale resolution configuration
     */
    protected HandlerInputAbstractLocaleResolver(HandlerInputLocaleResolutionConfiguration localeResolutionConfiguration) {
        super(localeResolutionConfiguration.getDefaultLocale());
        this.localeResolutionConfiguration = localeResolutionConfiguration;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }
}
