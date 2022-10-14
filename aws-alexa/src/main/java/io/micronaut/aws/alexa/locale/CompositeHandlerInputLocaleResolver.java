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
import io.micronaut.context.annotation.Primary;
import io.micronaut.core.annotation.NonNull;
import jakarta.inject.Singleton;

import java.util.Arrays;
import java.util.Locale;
import java.util.Optional;


/**
 * {@link Primary} {@link HandlerInputLocaleResolver} which evaluates every {@link HandlerInputLocaleResolver} by order to resolve a {@link java.util.Locale}.
 * @author Sergio del Amo
 * @since 3.10.0
 */
@Primary
@Singleton
public class CompositeHandlerInputLocaleResolver extends HandlerInputAbstractLocaleResolver {

    private final HandlerInputLocaleResolver[] localeResolvers;

    /**
     * @param localeResolvers Locale Resolvers
     * @param handlerInputLocaleResolutionConfiguration Locale Resolution configuration for HTTP Requests
     */
    public CompositeHandlerInputLocaleResolver(HandlerInputLocaleResolver[] localeResolvers,
                                               HandlerInputLocaleResolutionConfiguration handlerInputLocaleResolutionConfiguration) {
        super(handlerInputLocaleResolutionConfiguration);
        this.localeResolvers = localeResolvers;
    }

    @Override
    @NonNull
    public Optional<Locale> resolve(@NonNull HandlerInput request) {
        return Arrays.stream(localeResolvers)
                .map(resolver -> resolver.resolve(request))
                .filter(Optional::isPresent)
                .findFirst()
                .orElse(Optional.empty());
    }
}
