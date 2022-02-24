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
package io.micronaut.aws.xray.recorder;

import com.amazonaws.xray.contexts.SegmentContext;
import com.amazonaws.xray.contexts.SegmentContextResolver;
import io.micronaut.http.context.ServerRequestContext;

/**
 * If {@link ServerRequestContext::currentRequest} is able to resolve the request it returns an instance of {@link HttpRequestAttributeSegmentContext} otherwise returns null.
 * @author Sergio del Amo
 * @since 3.2.0
 */
public class HttpRequestAttributeSegmentContextResolver implements SegmentContextResolver {

    @Override
    public SegmentContext resolve() {
        return ServerRequestContext.currentRequest().isPresent() ?
                new HttpRequestAttributeSegmentContext() : null;
    }
}
