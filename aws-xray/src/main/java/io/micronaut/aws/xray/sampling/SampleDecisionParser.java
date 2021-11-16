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
import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.http.HttpRequest;

/**
 * Parses a Sampling Decision for an HTTP Request and a Sampling Response.
 * @author Sergio del Amo
 * @since 2.7.0
 */
@DefaultImplementation(DefaultSampleDecisionParser.class)
@FunctionalInterface
public interface SampleDecisionParser {

    /**
     *
     * @param request HTTP Request
     * @param samplingResponse Sampling response
     * @return Sample decision
     */
    @NonNull
    TraceHeader.SampleDecision sampleDecision(@NonNull HttpRequest<?> request,
                                              @NonNull SamplingResponse samplingResponse);
}
