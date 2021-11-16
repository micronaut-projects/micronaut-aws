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
package io.micronaut.aws.xray.tracing;

import com.amazonaws.xray.entities.Segment;
import com.amazonaws.xray.entities.TraceHeader;
import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;

import java.util.Optional;

/**
 * Parses a {@link TraceHeader} from {@link HttpRequest}.
 * @author Sergio del Amo
 * @since 2.7.0
 */
@DefaultImplementation(DefaultTraceHeaderParser.class)
public interface TraceHeaderParser {

    /**
     * Parses a {@link TraceHeader} from {@link HttpRequest}.
     * @param request HTTP Request
     * @return A {@link TraceHeader}
     */
    @NonNull
    Optional<TraceHeader> parseTraceHeader(@NonNull HttpRequest<?> request);

    /**
     *
     * @param segment X-Ray Segment
     * @param incomingHeader Incoming Tracing Header
     * @return Create a TraceHeader for the HTTP Response
     */
    @NonNull
    TraceHeader createResponseTraceHeader(@NonNull Segment segment, @Nullable TraceHeader incomingHeader);
}
