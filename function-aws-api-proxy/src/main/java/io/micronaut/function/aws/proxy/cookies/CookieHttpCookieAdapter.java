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

import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.cookie.Cookie;

import java.net.HttpCookie;

/**
 * Adapt from {@link HttpCookie} to {@link Cookie}.
 * @author Sergio del Amo
 * @since 4.3.0
 */
public class CookieHttpCookieAdapter implements Cookie {

    private final HttpCookie httpCookie;
    public CookieHttpCookieAdapter(HttpCookie httpCookie) {
        this.httpCookie = httpCookie;
    }

    @Override
    public @NonNull String getName() {
        return httpCookie.getName();
    }

    @Override
    public @NonNull String getValue() {
        return httpCookie.getValue();
    }

    @Override
    public @Nullable String getDomain() {
        return httpCookie.getDomain();
    }

    @Override
    public @Nullable String getPath() {
        return httpCookie.getPath();
    }

    @Override
    public boolean isHttpOnly() {
        return httpCookie.isHttpOnly();
    }

    @Override
    public boolean isSecure() {
        return httpCookie.getSecure();
    }

    @Override
    public long getMaxAge() {
        return httpCookie.getMaxAge();
    }

    @Override
    public @NonNull Cookie maxAge(long maxAge) {
        httpCookie.setMaxAge(maxAge);
        return this;
    }

    @Override
    public @NonNull Cookie value(@NonNull String value) {
        httpCookie.setValue(value);
        return this;
    }

    @Override
    public @NonNull Cookie domain(@Nullable String domain) {
        httpCookie.setValue(domain);
        return this;
    }

    @Override
    public @NonNull Cookie path(@Nullable String path) {
        httpCookie.setPath(path);
        return this;
    }

    @Override
    public @NonNull Cookie secure(boolean secure) {
        httpCookie.setSecure(secure);
        return this;
    }

    @Override
    public @NonNull Cookie httpOnly(boolean httpOnly) {
        httpCookie.setHttpOnly(httpOnly);
        return this;
    }

    @Override
    public int compareTo(Cookie o) {
        return 0;
    }
}
