/*
    Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
    except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
    the specific language governing permissions and limitations under the License.
 */
package io.micronaut.aws.alexa.httpserver.verifiers

import io.micronaut.aws.alexa.httpserver.AskHttpServerConstants
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject
import spock.util.environment.RestoreSystemProperties

import static org.junit.Assert.assertNull

/**
 * NOTICE: This test is a spock rewrite of com.amazon.ask.servlet.util.ServletUtilsTest https://github.com/alexa/alexa-skills-kit-sdk-for-java ask-sdk-servlet-support module
 */
class SkillRequestTimestampVerifierFactorySpec extends Specification {

    @Subject
    @Shared SkillRequestTimestampVerifierFactory factory = new SkillRequestTimestampVerifierFactory()

    void "null timestamp tolerance system property returns null"() {
        expect:
        !factory.timeStampToleranceSystemProperty()
    }

    @RestoreSystemProperties
    void "empty timestamp tolerance system property returns null"() {
        given:
        System.setProperty(AskHttpServerConstants.TIMESTAMP_TOLERANCE_SYSTEM_PROPERTY, "");

        expect:
        assertNull(factory.timeStampToleranceSystemProperty());
    }

    @RestoreSystemProperties
    void "whitespace only timestamp tolerance system property returns null"() {
        System.setProperty(AskHttpServerConstants.TIMESTAMP_TOLERANCE_SYSTEM_PROPERTY, "    ");
        assertNull(factory.timeStampToleranceSystemProperty());
    }

    @RestoreSystemProperties
    void "non numeric value throws exception timestamp tolerance system property"() {
        given:
        System.setProperty(AskHttpServerConstants.TIMESTAMP_TOLERANCE_SYSTEM_PROPERTY, "bar")

        when:
        factory.timeStampToleranceSystemProperty()

        then:
        thrown(IllegalArgumentException.class)
    }

    @RestoreSystemProperties
    void "numeric value parsed timestamp tolerance system property"() {
        given:
        System.setProperty(AskHttpServerConstants.TIMESTAMP_TOLERANCE_SYSTEM_PROPERTY, "1234");

        expect:
        factory.timeStampToleranceSystemProperty() == 1234L
    }
}
