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

import com.amazon.ask.model.IntentRequest
import com.amazon.ask.model.LaunchRequest
import com.amazon.ask.model.RequestEnvelope
import io.micronaut.aws.alexa.httpserver.AskHttpServerConstants
import io.micronaut.core.convert.ConversionService
import io.micronaut.http.MutableHttpHeaders
import io.micronaut.http.simple.SimpleHttpHeaders
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Unroll

import java.time.OffsetDateTime
import java.time.ZoneId
import java.util.concurrent.TimeUnit

/**
 * NOTICE: This test is a spock rewrite of com.amazon.ask.servlet.verifiers.SkillRequestTimestampVerifierTest https://github.com/alexa/alexa-skills-kit-sdk-for-java ask-sdk-servlet-support module
 */
class SkillRequestTimestampVerifierSpec extends Specification {

    private static final long TOLERANCE_IN_MILLIS = 2000;

    @Shared
    byte[] serializedRequestEnvelope = "".bytes

    @Shared
    MutableHttpHeaders headers = new SimpleHttpHeaders(ConversionService.SHARED)

    void "construct withNegativeTolerance throwsIllegalArgumentException"() {
        when:
        new SkillRequestTimestampVerifier(-1)

        then:
        thrown(IllegalArgumentException)
    }

    void "construct withToleranceBeyondMaximum throwsIllegalArgumentException"() {
        when:
        new SkillRequestTimestampVerifier(AskHttpServerConstants.MAXIMUM_TOLERANCE_MILLIS + 1)

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    void "verify currentDate no exception"() {
        when:

        verifier.verify(new HttpServerAlexaHttpRequest(headers, serializedRequestEnvelope, getRequestEnvelope(new Date())))

        then:
        noExceptionThrown()

        where:
        verifier << verifiers()
    }

    @Unroll
    void "verify recentOldDate no exception"() {
        given:
        Date d = new Date((System.currentTimeMillis() - TOLERANCE_IN_MILLIS / 2) as long )
        when:
        verifier.verify(new HttpServerAlexaHttpRequest(headers, serializedRequestEnvelope, getRequestEnvelope(d)))

        then:
        noExceptionThrown()

        where:
        verifier << verifiers()
    }

    @Unroll
    void "verify upcomingNewDate no exception"() {
        given:
        Date d = new Date((System.currentTimeMillis() - TOLERANCE_IN_MILLIS / 2) as long )

        when:
        verifier.verify(new HttpServerAlexaHttpRequest(headers, serializedRequestEnvelope, getRequestEnvelope(d)))

        then:
        noExceptionThrown()

        where:
        verifier << verifiers()
    }

    void "verify tooOldDate throws exception"() {
        given:
        Date d = new Date(System.currentTimeMillis() - TOLERANCE_IN_MILLIS * 2)

        when:
        verifier.verify(new HttpServerAlexaHttpRequest(headers, serializedRequestEnvelope, getRequestEnvelope(d)))

        then:
        thrown(SecurityException)

        where:
        verifier << verifiers()
    }

    void "verify tooNewDate throws exception"() {
        given:
        Date d = new Date(System.currentTimeMillis() + TOLERANCE_IN_MILLIS * 2)

        when:
        verifier.verify(new HttpServerAlexaHttpRequest(headers, serializedRequestEnvelope, getRequestEnvelope(d)))

        then:
        thrown(SecurityException)

        where:
        verifier << verifiers()
    }

    void "verify nullDate throws exception"() {
        when:
        verifier.verify(new HttpServerAlexaHttpRequest(headers, serializedRequestEnvelope, getRequestEnvelope(null)));

        then:
        thrown(SecurityException)

        where:
        verifier << verifiers()
    }

    @Unroll
    void "verify nullRequestEnvelope throws exception"() {
        when:
        verifier.verify(new HttpServerAlexaHttpRequest(headers, serializedRequestEnvelope, null));

        then:
        thrown(SecurityException)

        where:
        verifier << verifiers()
    }

    @Unroll
    void "verify nullRequest throws exception"() {
        when:
        verifier.verify(new HttpServerAlexaHttpRequest(headers, serializedRequestEnvelope, RequestEnvelope.builder().build()));

        then:
        thrown(SecurityException)

        where:
        verifier << verifiers()
    }

    @Unroll
    void "verify null Timestamp throws exception"() {
        when:
        verifier.verify(new HttpServerAlexaHttpRequest(headers, serializedRequestEnvelope, RequestEnvelope.builder().withRequest(IntentRequest.builder().build()).build()))

        then:
        thrown(SecurityException)

        where:
        verifier << verifiers()
    }

    void "construct with Null TimeUnit throws IllegalArgumentException"() {
        when:
        new SkillRequestTimestampVerifier((TOLERANCE_IN_MILLIS / 1000) as long, null);

        then:
        thrown(IllegalArgumentException)
    }

    @Unroll
    void "verify withGreaterRequestTimeDelta throws exception"() {
        when:
        verifier.verify(new HttpServerAlexaHttpRequest(headers, serializedRequestEnvelope, getRequestEnvelope(new Date(System.currentTimeMillis() + TOLERANCE_IN_MILLIS + 200))))

        then:
        thrown(SecurityException)

        where:
        verifier << verifiers()
    }


    List<SkillRequestTimestampVerifier> verifiers() {
        return Arrays.asList(new SkillRequestTimestampVerifier(TOLERANCE_IN_MILLIS), new SkillRequestTimestampVerifier((TOLERANCE_IN_MILLIS / 1000) as long, TimeUnit.SECONDS));
    }

    private RequestEnvelope getRequestEnvelope(Date timestamp) {
        return RequestEnvelope.builder().withRequest(LaunchRequest
                .builder()
                .withRequestId("rId")
                .withTimestamp(timestamp != null ? OffsetDateTime.ofInstant(timestamp.toInstant(), ZoneId.systemDefault()) : null)
                .withLocale("en-US")
                .build()).build();
    }
}
