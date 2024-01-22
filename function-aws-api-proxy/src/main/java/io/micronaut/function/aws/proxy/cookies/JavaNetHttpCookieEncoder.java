/*
 * Copyright 2017-2024 original authors
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
package io.micronaut.function.aws.proxy.cookies;

import io.micronaut.context.annotation.Secondary;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.cookie.Cookie;
import jakarta.inject.Singleton;

import java.net.HttpCookie;

/**
 * Implementation of {@link CookieEncoder} which uses {@link java.net.HttpCookie} to encode cookies.
 * @author Sergio del Amo
 * @since 4.4.0
 */
@Secondary
@Singleton
@Internal
public class JavaNetHttpCookieEncoder implements CookieEncoder {
    @Override
    @NonNull
    public String encode(@NonNull Cookie cookie) {
        HttpCookie httpCookie = CookieFactory.create(cookie);
        return httpCookie.toString();
    }
}
