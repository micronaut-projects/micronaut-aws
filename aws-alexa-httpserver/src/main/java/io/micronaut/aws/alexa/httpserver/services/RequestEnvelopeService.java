/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.aws.alexa.httpserver.services;

import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.ResponseEnvelope;
import edu.umd.cs.findbugs.annotations.NonNull;
import edu.umd.cs.findbugs.annotations.Nullable;
import io.micronaut.context.annotation.DefaultImplementation;

import javax.validation.constraints.NotNull;

/**
 * Process a {@link RequestEnvelope} and returns a {@link ResponseEnvelope} if it is capable of handling the request.
 * @author sdelamo
 * @since 2.0.0
 */
@FunctionalInterface
@DefaultImplementation(DefaultRequestEnvelopeService.class)
public interface RequestEnvelopeService {

    /**
     *
     * @param requestEnvelope The Request Envelope
     * @return A {@link ResponseEnvelope} after the request is processed or {@code null} if no handling is possible.
     */
    @Nullable
    ResponseEnvelope process(@NonNull @NotNull RequestEnvelope requestEnvelope);
}
