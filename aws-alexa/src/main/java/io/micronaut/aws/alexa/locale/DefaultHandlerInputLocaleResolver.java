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
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import jakarta.inject.Singleton;

import java.util.Locale;
import java.util.Optional;

/**
 * Resolves {@link Locale} from the {@link HandlerInput} request.
 * @see <a href="https://developer.amazon.com/en-US/docs/alexa/custom-skills/request-and-response-json-reference.html#request-locale">Request Locale</a>.
 * @author Sergio del Amo
 * @since 3.10.0
 */
@Singleton
public class DefaultHandlerInputLocaleResolver extends HandlerInputAbstractLocaleResolver {

    /**
     * @param localeResolutionConfiguration The locale resolution configuration
     */
    protected DefaultHandlerInputLocaleResolver(HandlerInputLocaleResolutionConfiguration localeResolutionConfiguration) {
        super(localeResolutionConfiguration);
    }

    @Override
    @NonNull
    public Optional<Locale> resolve(@NonNull HandlerInput input) {
        String languageTag = input.getRequest().getLocale();
        if (StringUtils.isEmpty(languageTag)) {
            return Optional.empty();
        }
        return Optional.of(Locale.forLanguageTag(languageTag));
    }
}
