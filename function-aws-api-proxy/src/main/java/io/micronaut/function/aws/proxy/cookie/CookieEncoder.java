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

import static io.micronaut.function.aws.proxy.cookie.CookieUtil.firstInvalidCookieNameOctet;
import static io.micronaut.function.aws.proxy.cookie.CookieUtil.firstInvalidCookieValueOctet;
import static io.micronaut.function.aws.proxy.cookie.CookieUtil.unwrapValue;

/**
 * Parent of Client and Server side cookie encoders.
 * Note: Forked from Netty `io.netty.handler.codec.http.cookie.CookieEncoder`
 */
@Internal
public abstract class CookieEncoder {

    protected final boolean strict;

    protected CookieEncoder(boolean strict) {
        this.strict = strict;
    }

    /**
     *
     * @param name cookie name
     * @param value cookie value
     */
    protected void validateCookie(String name, String value) {
        if (strict) {
            int pos = firstInvalidCookieNameOctet(name);
            if (pos >= 0) {
                throw new IllegalArgumentException("Cookie name contains an invalid char: " + name.charAt(pos));
            }

            CharSequence unwrappedValue = unwrapValue(value);
            if (unwrappedValue == null) {
                throw new IllegalArgumentException("Cookie value wrapping quotes are not balanced: " + value);
            }
            pos = firstInvalidCookieValueOctet(unwrappedValue);
            if (pos >= 0) {
                throw new IllegalArgumentException("Cookie value contains an invalid char: " +
                    unwrappedValue.charAt(pos));
            }
        }
    }
}
