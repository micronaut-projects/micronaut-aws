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
import io.micronaut.aws.xray.configuration.XRayConfiguration;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.http.HttpRequest;

import jakarta.inject.Singleton;
import java.util.Optional;

/**
 * {@link io.micronaut.context.annotation.DefaultImplementation} of {@link TraceHeaderParser}.
 * If {@link XRayConfiguration#acceptTraceIdHeaders()} is set to true, attempts to retrieve the Trace ID from X-Amzn-Trace-Id header.
 * @author Sergio del Amo
 * @since 2.7.0
 */
@Singleton
public class DefaultTraceHeaderParser implements TraceHeaderParser {

    private final XRayConfiguration xRayConfiguration;

    /**
     * Constructor.
     * @param xRayConfiguration X-Ray Configuration
     */
    public DefaultTraceHeaderParser(XRayConfiguration xRayConfiguration) {
        this.xRayConfiguration = xRayConfiguration;
    }

    @Override
    @NonNull
    public Optional<TraceHeader> parseTraceHeader(@NonNull HttpRequest<?> request) {
        String traceHeaderString = xRayConfiguration.acceptTraceIdHeaders() ? request.getHeaders().get(TraceHeader.HEADER_KEY) : null;
        if (null != traceHeaderString) {
            return Optional.of(TraceHeader.fromString(traceHeaderString));
        }
        return Optional.empty();
    }

    @Override
    @NonNull
    public TraceHeader createResponseTraceHeader(@NonNull Segment segment, @Nullable TraceHeader incomingHeader) {
        final TraceHeader responseHeader = new TraceHeader(segment.getTraceId());
        if (incomingHeader != null) {
            if (TraceHeader.SampleDecision.REQUESTED == incomingHeader.getSampled()) {
                responseHeader.setSampled(segment.isSampled() ? TraceHeader.SampleDecision.SAMPLED : TraceHeader.SampleDecision.NOT_SAMPLED);
            }
        }
        return responseHeader;
    }
}
