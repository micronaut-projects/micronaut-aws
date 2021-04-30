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
package io.micronaut.aws.xray.sampling;

import com.amazonaws.xray.entities.TraceHeader;
import com.amazonaws.xray.strategy.sampling.SamplingResponse;
import io.micronaut.aws.xray.tracing.TraceHeaderUtils;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.Optional;

/**
 * Utility methods around Sample decisions.
 * @author Sergio del Amo
 * @since 2.7.0
 */
public final class SampleDecisionUtils {

    private static final Logger LOG = LoggerFactory.getLogger(SampleDecisionUtils.class);

    SampleDecisionUtils() {
    }

    /**
     *
     * @param request HTTP Request
     * @param samplingResponse Sampling response
     * @return Sample decision
     */
    @NonNull
    public static TraceHeader.SampleDecision sampleDecision(@NonNull HttpRequest<?> request,
                                                            @NonNull SamplingResponse samplingResponse) {
        Optional<TraceHeader> incomingHeader = TraceHeaderUtils.getTraceHeader(request);
        TraceHeader.SampleDecision sampleDecision = incomingHeader.map(TraceHeader::getSampled)
                .orElseGet(() -> getSampleDecision(samplingResponse));
        if (TraceHeader.SampleDecision.REQUESTED.equals(sampleDecision) || TraceHeader.SampleDecision.UNKNOWN.equals(sampleDecision)) {
            return getSampleDecision(samplingResponse);
        }
        return sampleDecision;
    }

    /**
     *
     * @param sample Sampling response
     * @return Sampling decision
     */
    @NonNull
    private static TraceHeader.SampleDecision getSampleDecision(@NonNull SamplingResponse sample) {
        if (sample.isSampled()) {
            if (LOG.isTraceEnabled()) {
                LOG.trace("Sampling strategy decided SAMPLED.");
            }
            return TraceHeader.SampleDecision.SAMPLED;
        }
        if (LOG.isTraceEnabled()) {
            LOG.trace("Sampling strategy decided NOT_SAMPLED.");
        }
        return TraceHeader.SampleDecision.NOT_SAMPLED;
    }
}
