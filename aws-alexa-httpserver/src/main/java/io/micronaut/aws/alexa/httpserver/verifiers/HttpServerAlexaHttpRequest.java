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

/*
    Copyright 2019 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
    except in compliance with the License. A copy of the License is located at

        https://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
    the specific language governing permissions and limitations under the License.
 */

package io.micronaut.aws.alexa.httpserver.verifiers;

import io.micronaut.http.HttpHeaders;
import com.amazon.ask.model.RequestEnvelope;
import io.micronaut.aws.alexa.httpserver.AskHttpServerConstants;

/**
 * HTTP Server specific implementation of {@link AlexaHttpRequest}.
 *
 * NOTICE: This class is forked from com.amazon.ask.servlet.verifiers.ServletRequest found at https://github.com/alexa/alexa-skills-kit-sdk-for-java ask-sdk-servlet-support module
 *
 * @author sdelamo
 * @since 2.0.0
 */
public class HttpServerAlexaHttpRequest implements AlexaHttpRequest {

    /**
     * Serialized request envelope.
     */
    private final byte[] serializedRequestEnvelope;

    /**
     * De-serialized request envelope.
     */
    private final RequestEnvelope deserializedRequestEnvelope;

    /**
     * Base64 encoded signature.
     */
    private final String baseEncoded64Signature;

    /**
     * Certificate chain URL.
     */
    private final String signingCertificateChainUrl;

    /**
     * Constructor to build an instance of ServletRequest.
     * @param httpHeaders httpHeaders
     * @param serializedRequestEnvelope serialized request envelope.
     * @param deserializedRequestEnvelope de-serialized request envelope.
     */
    public HttpServerAlexaHttpRequest(final HttpHeaders httpHeaders,
                                      final byte[] serializedRequestEnvelope,
                                      final RequestEnvelope deserializedRequestEnvelope) {
        this.serializedRequestEnvelope = serializedRequestEnvelope;
        this.deserializedRequestEnvelope = deserializedRequestEnvelope;
        this.baseEncoded64Signature = httpHeaders.get(AskHttpServerConstants.SIGNATURE_REQUEST_HEADER);
        this.signingCertificateChainUrl = httpHeaders.get(AskHttpServerConstants.SIGNATURE_CERTIFICATE_CHAIN_URL_REQUEST_HEADER);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getBaseEncoded64Signature() {
        return baseEncoded64Signature;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getSigningCertificateChainUrl() {
        return signingCertificateChainUrl;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public byte[] getSerializedRequestEnvelope() {
        return serializedRequestEnvelope;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public RequestEnvelope getDeserializedRequestEnvelope() {
        return deserializedRequestEnvelope;
    }
}
