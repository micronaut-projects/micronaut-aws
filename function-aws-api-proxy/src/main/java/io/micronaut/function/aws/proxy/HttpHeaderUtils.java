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
package io.micronaut.function.aws.proxy;

import io.micronaut.core.annotation.Internal;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpHeaders;

/**
 * Internal utils class to normalize HTTP Headers.
 * @author Sergio del Amo
 * @since 3.10.6
 */
@Internal
final class HttpHeaderUtils {

    private HttpHeaderUtils() {
    }

    @NonNull
    static String normalizeHttpHeaderCase(@NonNull String headerName) {
        if (headerName.equalsIgnoreCase(HttpHeaders.ACCEPT)) {
            return HttpHeaders.ACCEPT;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCEPT_CH)) {
            return HttpHeaders.ACCEPT_CH;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCEPT_CH_LIFETIME)) {
            return HttpHeaders.ACCEPT_CH_LIFETIME;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCEPT_CHARSET)) {
            return HttpHeaders.ACCEPT_CHARSET;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCEPT_ENCODING)) {
            return HttpHeaders.ACCEPT_ENCODING;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCEPT_LANGUAGE)) {
            return HttpHeaders.ACCEPT_LANGUAGE;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCEPT_RANGES)) {
            return HttpHeaders.ACCEPT_RANGES;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCEPT_PATCH)) {
            return HttpHeaders.ACCEPT_PATCH;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS)) {
            return HttpHeaders.ACCESS_CONTROL_ALLOW_CREDENTIALS;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS)) {
            return HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS)) {
            return HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN)) {
            return HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS)) {
            return HttpHeaders.ACCESS_CONTROL_EXPOSE_HEADERS;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCESS_CONTROL_MAX_AGE)) {
            return HttpHeaders.ACCESS_CONTROL_MAX_AGE;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS)) {
            return HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD)) {
            return HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.AGE)) {
            return HttpHeaders.AGE;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ALLOW)) {
            return HttpHeaders.ALLOW;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.AUTHORIZATION)) {
            return HttpHeaders.AUTHORIZATION;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.AUTHORIZATION_INFO)) {
            return HttpHeaders.AUTHORIZATION_INFO;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.CACHE_CONTROL)) {
            return HttpHeaders.CACHE_CONTROL;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.CONNECTION)) {
            return HttpHeaders.CONNECTION;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_BASE)) {
            return HttpHeaders.CONTENT_BASE;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_DISPOSITION)) {
            return HttpHeaders.CONTENT_DISPOSITION;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_DPR)) {
            return HttpHeaders.CONTENT_DPR;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_ENCODING)) {
            return HttpHeaders.CONTENT_ENCODING;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LANGUAGE)) {
            return HttpHeaders.CONTENT_LANGUAGE;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LENGTH)) {
            return HttpHeaders.CONTENT_LENGTH;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_LOCATION)) {
            return HttpHeaders.CONTENT_LOCATION;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_TRANSFER_ENCODING)) {
            return HttpHeaders.CONTENT_TRANSFER_ENCODING;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_MD5)) {
            return HttpHeaders.CONTENT_MD5;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_RANGE)) {
            return HttpHeaders.CONTENT_RANGE;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.CONTENT_TYPE)) {
            return HttpHeaders.CONTENT_TYPE;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.COOKIE)) {
            return HttpHeaders.COOKIE;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.CROSS_ORIGIN_RESOURCE_POLICY)) {
            return HttpHeaders.CROSS_ORIGIN_RESOURCE_POLICY;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.DATE)) {
            return HttpHeaders.DATE;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.DEVICE_MEMORY)) {
            return HttpHeaders.DEVICE_MEMORY;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.DOWNLINK)) {
            return HttpHeaders.DOWNLINK;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.DPR)) {
            return HttpHeaders.DPR;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ECT)) {
            return HttpHeaders.ECT;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ETAG)) {
            return HttpHeaders.ETAG;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.EXPECT)) {
            return HttpHeaders.EXPECT;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.EXPIRES)) {
            return HttpHeaders.EXPIRES;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.FEATURE_POLICY)) {
            return HttpHeaders.FEATURE_POLICY;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.FORWARDED)) {
            return HttpHeaders.FORWARDED;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.FROM)) {
            return HttpHeaders.FROM;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.HOST)) {
            return HttpHeaders.HOST;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.IF_MATCH)) {
            return HttpHeaders.IF_MATCH;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.IF_MODIFIED_SINCE)) {
            return HttpHeaders.IF_MODIFIED_SINCE;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.IF_NONE_MATCH)) {
            return HttpHeaders.IF_NONE_MATCH;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.IF_RANGE)) {
            return HttpHeaders.IF_RANGE;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.IF_UNMODIFIED_SINCE)) {
            return HttpHeaders.IF_UNMODIFIED_SINCE;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.LAST_MODIFIED)) {
            return HttpHeaders.LAST_MODIFIED;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.LINK)) {
            return HttpHeaders.LINK;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.LOCATION)) {
            return HttpHeaders.LOCATION;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.MAX_FORWARDS)) {
            return HttpHeaders.MAX_FORWARDS;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.ORIGIN)) {
            return HttpHeaders.ORIGIN;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.PRAGMA)) {
            return HttpHeaders.PRAGMA;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.PROXY_AUTHENTICATE)) {
            return HttpHeaders.PROXY_AUTHENTICATE;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.PROXY_AUTHORIZATION)) {
            return HttpHeaders.PROXY_AUTHORIZATION;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.RANGE)) {
            return HttpHeaders.RANGE;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.REFERER)) {
            return HttpHeaders.REFERER;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.REFERRER_POLICY)) {
            return HttpHeaders.REFERRER_POLICY;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.RETRY_AFTER)) {
            return HttpHeaders.RETRY_AFTER;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.RTT)) {
            return HttpHeaders.RTT;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.SAVE_DATA)) {
            return HttpHeaders.SAVE_DATA;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.SEC_WEBSOCKET_KEY1)) {
            return HttpHeaders.SEC_WEBSOCKET_KEY1;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.SEC_WEBSOCKET_KEY2)) {
            return HttpHeaders.SEC_WEBSOCKET_KEY2;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.SEC_WEBSOCKET_LOCATION)) {
            return HttpHeaders.SEC_WEBSOCKET_LOCATION;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.SEC_WEBSOCKET_ORIGIN)) {
            return HttpHeaders.SEC_WEBSOCKET_ORIGIN;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.SEC_WEBSOCKET_PROTOCOL)) {
            return HttpHeaders.SEC_WEBSOCKET_PROTOCOL;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.SEC_WEBSOCKET_VERSION)) {
            return HttpHeaders.SEC_WEBSOCKET_VERSION;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.SEC_WEBSOCKET_KEY)) {
            return HttpHeaders.SEC_WEBSOCKET_KEY;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.SEC_WEBSOCKET_ACCEPT)) {
            return HttpHeaders.SEC_WEBSOCKET_ACCEPT;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.SERVER)) {
            return HttpHeaders.SERVER;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.SET_COOKIE)) {
            return HttpHeaders.SET_COOKIE;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.SET_COOKIE2)) {
            return HttpHeaders.SET_COOKIE2;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.SOURCE_MAP)) {
            return HttpHeaders.SOURCE_MAP;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.TE)) {
            return HttpHeaders.TE;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.TRAILER)) {
            return HttpHeaders.TRAILER;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.TRANSFER_ENCODING)) {
            return HttpHeaders.TRANSFER_ENCODING;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.UPGRADE)) {
            return HttpHeaders.UPGRADE;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.USER_AGENT)) {
            return HttpHeaders.USER_AGENT;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.VARY)) {
            return HttpHeaders.VARY;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.VIA)) {
            return HttpHeaders.VIA;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.VIEWPORT_WIDTH)) {
            return HttpHeaders.VIEWPORT_WIDTH;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.WARNING)) {
            return HttpHeaders.WARNING;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.WEBSOCKET_LOCATION)) {
            return HttpHeaders.WEBSOCKET_LOCATION;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.WEBSOCKET_ORIGIN)) {
            return HttpHeaders.WEBSOCKET_ORIGIN;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.WEBSOCKET_PROTOCOL)) {
            return HttpHeaders.WEBSOCKET_PROTOCOL;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.WIDTH)) {
            return HttpHeaders.WIDTH;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.WWW_AUTHENTICATE)) {
            return HttpHeaders.WWW_AUTHENTICATE;
        } else if (headerName.equalsIgnoreCase(HttpHeaders.X_AUTH_TOKEN)) {
            return HttpHeaders.X_AUTH_TOKEN;
        }
        return headerName;
    }
}
