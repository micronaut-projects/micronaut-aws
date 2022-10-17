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

import io.micronaut.aws.alexa.conf.AlexaSkillConfigurationProperties;
import io.micronaut.context.annotation.ConfigurationProperties;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;

import java.util.Locale;
import java.util.Optional;

/**
 * {@link ConfigurationProperties} implementation of {@link HandlerInputLocaleResolutionConfiguration}.
 * @author Sergio del Amo
 * @since 3.10.0
 */
@ConfigurationProperties(HandlerInputLocaleResolutionConfigurationProperties.PREFIX)
public class HandlerInputLocaleResolutionConfigurationProperties implements HandlerInputLocaleResolutionConfiguration {
    public static final String PREFIX = AlexaSkillConfigurationProperties.PREFIX + ".locale-resolution";

    /**
     * The default locale.
     */
    @SuppressWarnings("WeakerAccess")
    public static final String DEFAULT_LOCALE = "en-US";

    private Locale fixed;
    private Locale defaultLocale = Locale.forLanguageTag(DEFAULT_LOCALE);

    /**
     * @return The fixed locale
     */
    @NonNull
    public Optional<Locale> getFixed() {
        return Optional.ofNullable(fixed);
    }

    /**
     * Sets the fixed locale. Any of ar-SA, de-DE, en-AU, en-CA, en-GB, en-IN, en-US, es-ES, es-MX, es-US, fr-CA, fr-FR, hi-IN, it-IT, ja-JP, pt-BR
     *
     * @param fixed The fixed locale
     */
    public void setFixed(@Nullable Locale fixed) {
        this.fixed = fixed;
    }

    /**
     * @return The locale to be used if one cannot be resolved.
     */
    @Override
    @NonNull
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    /**
     * Sets the locale that will be used if the locale cannot be
     * resolved through any means. Defaults to {@value #DEFAULT_LOCALE}.
     *
     * @param defaultLocale The default locale.
     */
    public void setDefaultLocale(@NonNull Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

}
