/*
    Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
    except in compliance with the License. A copy of the License is located at

        https://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
    the specific language governing permissions and limitations under the License.
 */

package io.micronaut.aws.alexa.httpserver.verifiers

import io.micronaut.aws.alexa.httpserver.AskHttpServerConstants
import spock.lang.Shared
import spock.lang.Specification
import spock.util.environment.RestoreSystemProperties

/**
 * NOTICE: This test is a spock rewrite of com.amazon.ask.servlet.util.ServletUtilsTest https://github.com/alexa/alexa-skills-kit-sdk-for-java ask-sdk-servlet-support module
 */
class RequestSignatureCheckSystemPropertyConditionSpec extends Specification {

    @Shared
    RequestSignatureCheckSystemPropertyCondition condition = new RequestSignatureCheckSystemPropertyCondition()

    void "null request signature check system property returns false"() {
        expect:
        !condition.matches(null)
    }

    @RestoreSystemProperties
    void "empty request signature check system property returns false"() {
        given:
        System.setProperty(AskHttpServerConstants.DISABLE_REQUEST_SIGNATURE_CHECK_SYSTEM_PROPERTY, "");

        expect:
        !condition.matches()
    }

    @RestoreSystemProperties
    void "whitespace only request signature check system property returns false"() {
        given:
        System.setProperty(AskHttpServerConstants.DISABLE_REQUEST_SIGNATURE_CHECK_SYSTEM_PROPERTY, "    ");

        expect:
        !condition.matches(null)
    }

    @RestoreSystemProperties
    void "string value request signature check system property returns false"() {
        given:
        System.setProperty(AskHttpServerConstants.DISABLE_REQUEST_SIGNATURE_CHECK_SYSTEM_PROPERTY, "bar");

        expect:
        !condition.matches(null)
    }

    @RestoreSystemProperties
    void "false value request signature check system property returns false"() {
        given:
        System.setProperty(AskHttpServerConstants.DISABLE_REQUEST_SIGNATURE_CHECK_SYSTEM_PROPERTY, "false");

        expect:
        !condition.matches(null)
    }

    @RestoreSystemProperties
    void "true value request signature check system property returns true"() {
        given:
        System.setProperty(AskHttpServerConstants.DISABLE_REQUEST_SIGNATURE_CHECK_SYSTEM_PROPERTY, "true");

        expect:
        condition.matches(null)
    }
}
