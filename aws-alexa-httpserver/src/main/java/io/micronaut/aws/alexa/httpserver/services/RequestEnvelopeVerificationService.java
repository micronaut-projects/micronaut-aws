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

import com.amazon.ask.exception.AskSdkException;
import com.amazon.ask.model.RequestEnvelope;
import io.micronaut.context.annotation.DefaultImplementation;
import io.micronaut.http.HttpHeaders;

/**
 * Verifies a {@link RequestEnvelope}. If invalid an exception is thrown.
 * @author sdelamo
 * @since 2.0.0
 */
@FunctionalInterface
@DefaultImplementation(DefaultRequestEnvelopeVerificationService.class)
public interface RequestEnvelopeVerificationService {

    /**
     *
     * @param httpHeaders HTTP Headers of the request
     * @param serializedRequestEnvelope byte array of the request envelope
     * @param requestEnvelope Request Envelope
     * @throws SecurityException Raised if the signature of the request cannot be matched.
     * @throws AskSdkException ASK SDK exception raised by teh verifiers
     */
    void verify(HttpHeaders httpHeaders,
                byte[] serializedRequestEnvelope,
                RequestEnvelope requestEnvelope) throws SecurityException, AskSdkException;
}
