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
package io.micronaut.aws.xray.decorators;

import com.amazonaws.xray.entities.Segment;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;

/**
 * Contract to decorate segments being created by the {@link io.micronaut.aws.xray.server.XRayHttpServerFilter}.
 * Useful to add custom annotations.
 * @author Sergio del Amo
 * @since 2.7.0
 */
@FunctionalInterface
public interface SegmentDecorator {

    /**
     *
     * @param segment The X-Ray Segment
     * @param request The HTTP Requests
     */
    void decorate(@NonNull Segment segment, @NonNull HttpRequest<?> request);
}
