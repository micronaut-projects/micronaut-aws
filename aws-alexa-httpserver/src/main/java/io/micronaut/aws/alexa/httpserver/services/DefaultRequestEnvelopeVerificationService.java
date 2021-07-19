/*
 * Copyright 2017-2020 original authors
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
package io.micronaut.aws.alexa.httpserver.services;

import com.amazon.ask.exception.AskSdkException;
import com.amazon.ask.model.RequestEnvelope;
import io.micronaut.aws.alexa.httpserver.verifiers.AlexaHttpRequest;
import io.micronaut.aws.alexa.httpserver.verifiers.HttpServerAlexaHttpRequest;
import io.micronaut.aws.alexa.httpserver.verifiers.SkillServletVerifier;
import io.micronaut.http.HttpHeaders;

import jakarta.inject.Singleton;
import java.util.List;

/**
 * {@link io.micronaut.context.annotation.DefaultImplementation} of {@link RequestEnvelopeVerificationService}.
 */
@Singleton
public class DefaultRequestEnvelopeVerificationService implements RequestEnvelopeVerificationService {
    /**
     * List of {@link SkillServletVerifier}.
     */
    private final List<SkillServletVerifier> verifiers;

    /**
     *
     * @param verifiers Skill Verifiers
     */
    public DefaultRequestEnvelopeVerificationService(List<SkillServletVerifier> verifiers) {
        this.verifiers = verifiers;
    }

    @Override
    public void verify(HttpHeaders httpHeaders,
                       byte[] serializedRequestEnvelope,
                       RequestEnvelope requestEnvelope) throws SecurityException, AskSdkException {
        // Verify the authenticity of the request by executing configured verifiers.
        final AlexaHttpRequest r = new HttpServerAlexaHttpRequest(httpHeaders, serializedRequestEnvelope, requestEnvelope);
        for (SkillServletVerifier verifier : verifiers) {
            verifier.verify(r);
        }
    }
}
