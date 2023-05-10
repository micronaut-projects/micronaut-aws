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
package io.micronaut.aws.function.apigatewayproxy;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ArgumentConversionContext;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.http.netty.cookies.NettyCookie;
import io.netty.handler.codec.http.cookie.ServerCookieDecoder;

import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * Implementation of {@link Cookies} for serverless.
 *
 */
@Internal
public class AwsCookies implements Cookies {

    private final ConversionService conversionService;
    private final Map<CharSequence, Cookie> cookies;

    /**
     * @param path              The path
     * @param headers      The Netty HTTP headers
     * @param conversionService The conversion service
     */
    public AwsCookies(String path, HttpHeaders headers, ConversionService conversionService) {
        this.conversionService = conversionService;
        String value = headers.get(HttpHeaders.COOKIE);
        if (value != null) {
            cookies = new LinkedHashMap<>(10);
            Set<io.netty.handler.codec.http.cookie.Cookie> nettyCookies = ServerCookieDecoder.STRICT.decode(value);
            for (io.netty.handler.codec.http.cookie.Cookie nettyCookie : nettyCookies) {
                String cookiePath = nettyCookie.path();
                if (cookiePath != null) {
                    if (path.startsWith(cookiePath)) {
                        cookies.put(nettyCookie.name(), new NettyCookie(nettyCookie));
                    }
                } else {
                    cookies.put(nettyCookie.name(), new NettyCookie(nettyCookie));
                }
            }
        } else {
            cookies = Collections.emptyMap();
        }
    }

    @Override
    public Set<Cookie> getAll() {
        return new HashSet<>(cookies.values());
    }

    @Override
    public Optional<Cookie> findCookie(CharSequence name) {
        Cookie cookie = cookies.get(name);
        return cookie != null ? Optional.of(cookie) : Optional.empty();
    }

    @Override
    public <T> Optional<T> get(CharSequence name, Class<T> requiredType) {
        if (requiredType == Cookie.class || requiredType == Object.class) {
            //noinspection unchecked
            return (Optional<T>) findCookie(name);
        } else {
            return findCookie(name).flatMap((cookie -> conversionService.convert(cookie.getValue(), requiredType)));
        }
    }

    @Override
    public <T> Optional<T> get(CharSequence name, ArgumentConversionContext<T> conversionContext) {
        return findCookie(name).flatMap((cookie -> conversionService.convert(cookie.getValue(), conversionContext)));
    }

    @Override
    public Collection<Cookie> values() {
        return Collections.unmodifiableCollection(cookies.values());
    }
}
