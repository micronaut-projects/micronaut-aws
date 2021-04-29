/*
 * Copyright 2017-2021 original authors
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
package io.micronaut.aws.xray.strategy;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.server.util.HttpHostResolver;

import javax.inject.Singleton;

/**
 * Uses as segment name the host name resolved by {@link HttpHostResolver}.
 * @author Sergio del Amo
 * @since 2.7.0
 */
@Requires(classes = HttpHostResolver.class)
@Requires(beans = HttpHostResolver.class)
@Singleton
public class HttpHostNamingStrategy implements SegmentNamingStrategy {
    public static final int ORDER = FixedSegmentNamingStrategy.ORDER + 100;

    private final HttpHostResolver httpHostResolver;

    /**
     *
     * @param httpHostResolver HTTP Host Resolver
     */
    public HttpHostNamingStrategy(HttpHostResolver httpHostResolver) {
        this.httpHostResolver = httpHostResolver;
    }

    @Override
    public int getOrder() {
        return ORDER;
    }

    @Override
    @NonNull
    public String nameForRequest(@NonNull HttpRequest<?> request) {
        return httpHostResolver.resolve(request);
    }
}
