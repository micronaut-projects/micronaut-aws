/*
 * Copyright 2017-2019 original authors
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
/*
    Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
    except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
    the specific language governing permissions and limitations under the License.
 */

package io.micronaut.aws.alexa.httpserver.controllers;

import com.amazon.ask.model.RequestEnvelope;
import com.amazon.ask.model.ResponseEnvelope;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.aws.alexa.httpserver.AskHttpServerConstants;
import io.micronaut.aws.alexa.httpserver.conf.AlexaControllerConfigurationProperties;
import io.micronaut.aws.alexa.httpserver.services.RequestEnvelopeService;
import io.micronaut.aws.alexa.httpserver.services.RequestEnvelopeVerificationService;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

/**
 * NOTICE: This class is inspired in com.amazon.ask.servlet.SkillServlet forked from https://github.com/alexa/alexa-skills-kit-sdk-for-java ask-sdk-servlet-support module
 *
 * <p>
 * This class takes care of the JSON serialization / deserialization of the HTTP body and the
 * invocation of the right method of the provided {@code Skill} . It also handles sending back
 * modified session attributes, user attributes and authentication tokens when needed and handles
 * exception cases.
 * </p>
 *
 * @author sdelamo
 * @since 2.0.0.
 */
@Requires(beans = ObjectMapper.class)
@Requires(property = AlexaControllerConfigurationProperties.PREFIX + ".enabled", notEquals = StringUtils.FALSE)
@Controller("${" + AlexaControllerConfigurationProperties.PREFIX + ".path:/alexa}")
public class SkillController {
    /**
     * Logger mechanism to log data for debugging purposes.
     */
    private static final Logger LOG = LoggerFactory.getLogger(SkillController.class);

    /**
     * The serialization runtime associates with each serializable class a version number, called a serialVersionUID,
     * which is used during deserialization to verify that the sender and receiver of a serialized object have loaded
     * classes for that object that are compatible with respect to serialization.
     */
    private static final long serialVersionUID = 3257254794185762002L;

    /**
     * Jackson Mapper.
     */
    private final ObjectMapper objectMapper;

    private final RequestEnvelopeVerificationService requestEnvelopeVerificationService;
    private final RequestEnvelopeService requestEnvelopeService;

    /**
     * Constructor to build an instance of SkillServlet.
     *
     * @param objectMapper Jackson Object Mapper
     * @param requestEnvelopeVerificationService Request Envelope verification service
     * @param requestEnvelopeService Request Envelope Service
     */
    public SkillController(ObjectMapper objectMapper,
                           RequestEnvelopeVerificationService requestEnvelopeVerificationService,
                           RequestEnvelopeService requestEnvelopeService) {
        this.objectMapper = objectMapper;
        this.requestEnvelopeVerificationService = requestEnvelopeVerificationService;
        this.requestEnvelopeService = requestEnvelopeService;
    }

    /**
     * Handles a POST request. Based on the request parameters, invokes the right method on the {@code Skill}.
     *
     * @param httpHeaders HTTP Headers
     * @param body HTTP Request Body byte array
     * @return response object that contains the response the servlet sends to the client
     */
    @Post
    public HttpResponse doPost(HttpHeaders httpHeaders,
                               @Body String body) {
        try {
            byte[] serializedRequestEnvelope = body.getBytes(AskHttpServerConstants.CHARACTER_ENCODING);
            final RequestEnvelope requestEnvelope = objectMapper.readValue(serializedRequestEnvelope, RequestEnvelope.class);
            requestEnvelopeVerificationService.verify(httpHeaders, serializedRequestEnvelope, requestEnvelope);
            ResponseEnvelope responseEnvelope = requestEnvelopeService.process(requestEnvelope);
            if (responseEnvelope != null) {
                return HttpResponse.ok(responseEnvelope);
            }

        } catch (IOException e) {

            if (LOG.isErrorEnabled()) {
                LOG.error("Unable to parse a byte array to RequestEnvelope");
            }
        }
        return HttpResponse.badRequest();
    }
}
