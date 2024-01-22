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

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.cookie.Cookie;

import java.net.HttpCookie;

@Internal
public class CookieFactory {

    @NonNull
    public static HttpCookie create(@NonNull Cookie cookie) {
        HttpCookie httpCookie = new HttpCookie(cookie.getName(), cookie.getValue());
        if (StringUtils.isNotEmpty(cookie.getDomain())) {
            httpCookie.setDomain(cookie.getDomain());
        }
        if (StringUtils.isNotEmpty(cookie.getPath())) {
            httpCookie.setPath(cookie.getPath());
        }
        httpCookie.setHttpOnly(cookie.isHttpOnly());
        httpCookie.setSecure(cookie.isSecure());
        httpCookie.setMaxAge(cookie.getMaxAge());
        return httpCookie;
    }
}
