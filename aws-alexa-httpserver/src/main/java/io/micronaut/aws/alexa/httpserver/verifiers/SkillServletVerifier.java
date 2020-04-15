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

package io.micronaut.aws.alexa.httpserver.verifiers;

/**
 * NOTICE: This class is forked from https://github.com/alexa/alexa-skills-kit-sdk-for-java ask-sdk-servlet-support module.
 *
 * Verifiers are run against incoming requests to verify authenticity and integrity of the request before processing it.
 *
 * @author sdelamo
 * @since 2.0.0
 */
public interface SkillServletVerifier {

    /**
     * Verifies an incoming request.
     *
     * @param alexaHttpRequest request performed by Alexa
     * @throws SecurityException if verification fails.
     */
    void verify(AlexaHttpRequest alexaHttpRequest) throws SecurityException;

}
