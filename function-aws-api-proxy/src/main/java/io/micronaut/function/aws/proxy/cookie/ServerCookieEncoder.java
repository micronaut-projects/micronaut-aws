/*
 * Copyright 2015 The Netty Project
 *
 * The Netty Project licenses this file to you under the Apache License,
 * version 2.0 (the "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at:
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations
 * under the License.
 */
package io.micronaut.function.aws.proxy.cookie;

import io.micronaut.core.annotation.Internal;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.SameSite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import static io.micronaut.function.aws.proxy.cookie.ObjectUtil.checkNotNull;
import static io.micronaut.function.aws.proxy.cookie.CookieUtil.add;
import static io.micronaut.function.aws.proxy.cookie.CookieUtil.addQuoted;
import static io.micronaut.function.aws.proxy.cookie.CookieUtil.stringBuffer;
import static io.micronaut.function.aws.proxy.cookie.CookieUtil.unwrapValue;
import static io.micronaut.function.aws.proxy.cookie.CookieUtil.stripTrailingSeparator;

/**
 * A <a href="https://tools.ietf.org/html/rfc6265">RFC6265</a> compliant cookie encoder to be used server side,
 * so some fields are sent (Version is typically ignored).
 *
 * As Netty's Cookie merges Expires and MaxAge into one single field, only Max-Age field is sent.
 *
 * Note that multiple cookies must be sent as separate "Set-Cookie" headers.
 *
 * <pre>
 * // Example
 * HttpResponse res = ...;
 * res.setHeader("Set-Cookie", {@link ServerCookieEncoder}.encode("JSESSIONID", "1234"));
 * </pre>
 *
 * Note: Forked from Netty `io.netty.handler.codec.http.cookie.ServerCookieEncoder`
 */
@Internal
public final class ServerCookieEncoder extends CookieEncoder {
    /**
     * Strict encoder that validates that name and value chars are in the valid scope
     * defined in RFC6265, and (for methods that accept multiple cookies) that only
     * one cookie is encoded with any given name. (If multiple cookies have the same
     * name, the last one is the one that is encoded.)
     */
    public static final ServerCookieEncoder STRICT = new ServerCookieEncoder(true);

    /**
     * Lax instance that doesn't validate name and value, and that allows multiple
     * cookies with the same name.
     */
    public static final ServerCookieEncoder LAX = new ServerCookieEncoder(false);

    private static final Logger LOG = LoggerFactory.getLogger(ServerCookieEncoder.class);

    private ServerCookieEncoder(boolean strict) {
        super(strict);
    }

    /**
     * The class `io.netty.handler.codec.http.cookie.Cookie` has a `wrap()` method.
     * This implements wrap as in `io.netty.handler.codec.http.cookie.CookieDecoder`
     *
     * Returns true if the raw value of this `io.netty.handler.codec.http.cookie.Cookie`,
     * was wrapped with double quotes in original Set-Cookie header.
     *
     * @return If the value of this `io.netty.handler.codec.http.cookie.Cookie` is to be wrapped
     */
    private static boolean isWrap(Cookie cookie) {
        CharSequence unwrappedValue = unwrapValue(cookie.getValue());
        if (unwrappedValue == null) {
            LOG.debug("returning false for wrap cookie because starting quotes are not properly balanced in '{}'",
                unwrappedValue);
            return false;
        }
        return unwrappedValue.length() != cookie.getValue().length();
    }

    /**
     * Encodes the specified cookie into a Set-Cookie header value.
     *
     * @param cookie the cookie
     * @return a single Set-Cookie header value
     */
    public String encode(Cookie cookie) {
        final String name = checkNotNull(cookie, "cookie").getName();
        final String value = cookie.getValue() != null ? cookie.getValue() : "";

        validateCookie(name, value);

        StringBuffer buf = stringBuffer();

        if (isWrap(cookie)) {
            addQuoted(buf, name, value);
        } else {
            add(buf, name, value);
        }

        if (cookie.getMaxAge() != Long.MIN_VALUE) {
            add(buf, CookieHeaderNames.MAX_AGE, cookie.getMaxAge());
            Date expires = new Date(cookie.getMaxAge() * 1000 + System.currentTimeMillis());
            buf.append(CookieHeaderNames.EXPIRES);
            buf.append('=');
            DateFormatter.append(expires, buf);
            buf.append(';');
            buf.append(HttpConstants.SP_CHAR);
        }

        if (cookie.getPath() != null) {
            add(buf, CookieHeaderNames.PATH, cookie.getPath());
        }

        if (cookie.getDomain() != null) {
            add(buf, CookieHeaderNames.DOMAIN, cookie.getDomain());
        }
        if (cookie.isSecure()) {
            add(buf, CookieHeaderNames.SECURE);
        }
        if (cookie.isHttpOnly()) {
            add(buf, CookieHeaderNames.HTTPONLY);
        }
        cookie.getSameSite()
            .map(SameSite::name)
            .ifPresent(sameSiteName ->
            add(buf, CookieHeaderNames.SAMESITE, sameSiteName));

        return stripTrailingSeparator(buf);
    }

    /** Deduplicate a list of encoded cookies by keeping only the last instance with a given name.
     *
     * @param encoded The list of encoded cookies.
     * @param nameToLastIndex A map from cookie name to index of last cookie instance.
     * @return The encoded list with all but the last instance of a named cookie.
     */
    private static List<String> dedup(List<String> encoded, Map<String, Integer> nameToLastIndex) {
        boolean[] isLastInstance = new boolean[encoded.size()];
        for (int idx : nameToLastIndex.values()) {
            isLastInstance[idx] = true;
        }
        List<String> dedupd = new ArrayList<String>(nameToLastIndex.size());
        for (int i = 0, n = encoded.size(); i < n; i++) {
            if (isLastInstance[i]) {
                dedupd.add(encoded.get(i));
            }
        }
        return dedupd;
    }

    /**
     * Batch encodes cookies into Set-Cookie header values.
     *
     * @param cookies a bunch of cookies
     * @return the corresponding bunch of Set-Cookie headers
     */
    public List<String> encode(Cookie... cookies) {
        if (checkNotNull(cookies, "cookies").length == 0) {
            return Collections.emptyList();
        }

        List<String> encoded = new ArrayList<String>(cookies.length);
        Map<String, Integer> nameToIndex = strict && cookies.length > 1 ? new HashMap<String, Integer>() : null;
        boolean hasDupdName = false;
        for (int i = 0; i < cookies.length; i++) {
            Cookie c = cookies[i];
            encoded.add(encode(c));
            if (nameToIndex != null) {
                hasDupdName |= nameToIndex.put(c.getName(), i) != null;
            }
        }
        return hasDupdName ? dedup(encoded, nameToIndex) : encoded;
    }

    /**
     * Batch encodes cookies into Set-Cookie header values.
     *
     * @param cookies a bunch of cookies
     * @return the corresponding bunch of Set-Cookie headers
     */
    public List<String> encode(Collection<? extends Cookie> cookies) {
        if (checkNotNull(cookies, "cookies").isEmpty()) {
            return Collections.emptyList();
        }

        List<String> encoded = new ArrayList<String>(cookies.size());
        Map<String, Integer> nameToIndex = strict && cookies.size() > 1 ? new HashMap<String, Integer>() : null;
        int i = 0;
        boolean hasDupdName = false;
        for (Cookie c : cookies) {
            encoded.add(encode(c));
            if (nameToIndex != null) {
                hasDupdName |= nameToIndex.put(c.getName(), i++) != null;
            }
        }
        return hasDupdName ? dedup(encoded, nameToIndex) : encoded;
    }

    /**
     * Batch encodes cookies into Set-Cookie header values.
     *
     * @param cookies a bunch of cookies
     * @return the corresponding bunch of Set-Cookie headers
     */
    public List<String> encode(Iterable<? extends Cookie> cookies) {
        Iterator<? extends Cookie> cookiesIt = checkNotNull(cookies, "cookies").iterator();
        if (!cookiesIt.hasNext()) {
            return Collections.emptyList();
        }

        List<String> encoded = new ArrayList<String>();
        Cookie firstCookie = cookiesIt.next();
        Map<String, Integer> nameToIndex = strict && cookiesIt.hasNext() ? new HashMap<String, Integer>() : null;
        int i = 0;
        encoded.add(encode(firstCookie));
        boolean hasDupdName = nameToIndex != null && nameToIndex.put(firstCookie.getName(), i++) != null;
        while (cookiesIt.hasNext()) {
            Cookie c = cookiesIt.next();
            encoded.add(encode(c));
            if (nameToIndex != null) {
                hasDupdName |= nameToIndex.put(c.getName(), i++) != null;
            }
        }
        return hasDupdName ? dedup(encoded, nameToIndex) : encoded;
    }
}
