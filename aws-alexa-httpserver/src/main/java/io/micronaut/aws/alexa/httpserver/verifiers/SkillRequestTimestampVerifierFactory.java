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
package io.micronaut.aws.alexa.httpserver.verifiers;

import io.micronaut.aws.alexa.conf.AlexaSkillConfigurationProperties;
import io.micronaut.aws.alexa.httpserver.AskHttpServerConstants;
import io.micronaut.context.annotation.Factory;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;

import javax.inject.Singleton;

/**
 * NOTICE: The method {@link SkillRequestTimestampVerifierFactory#timeStampToleranceSystemProperty()} is forked from https://github.com/alexa/alexa-skills-kit-sdk-for-java ask-sdk-servlet-support module.
 *
 * {@link Factory} to instantiate a request timestamp {@link SkillServletVerifier}.
 */
@Requires(property = AlexaSkillConfigurationProperties.PREFIX + ".verifiers.timestamp", notEquals = StringUtils.FALSE)
@Factory
public class SkillRequestTimestampVerifierFactory {

    /**
     *
     * @return Creates a {@link SkillRequestTimestampVerifier} with a tolerance in milliseconds which is specified with
     * the system property {@value AskHttpServerConstants#TIMESTAMP_TOLERANCE_SYSTEM_PROPERTY} or the default value {@value AskHttpServerConstants#DEFAULT_TOLERANCE_MILLIS}.
     */
    @Singleton
    public SkillServletVerifier createSkillRequestTimestampVerifier() {
        Long timestampToleranceProperty = timeStampToleranceSystemProperty();
        return new SkillRequestTimestampVerifier(timestampToleranceProperty != null
                ? timestampToleranceProperty : AskHttpServerConstants.DEFAULT_TOLERANCE_MILLIS);
    }

    /**
     * Returns the value of the {@link AskHttpServerConstants#TIMESTAMP_TOLERANCE_SYSTEM_PROPERTY} JVM system property as a {@link Long},
     * or returns null if the property is empty.
     * @return value of the {@link AskHttpServerConstants#TIMESTAMP_TOLERANCE_SYSTEM_PROPERTY} system property as a {@link Long}, or null
     * if the property is empty.
     * @throws IllegalArgumentException if the system property is present but not parseable as a Long.
     */
    public static Long timeStampToleranceSystemProperty() {
        String timestampToleranceAsString = System.getProperty(AskHttpServerConstants.TIMESTAMP_TOLERANCE_SYSTEM_PROPERTY);
        if (timestampToleranceAsString != null && !timestampToleranceAsString.trim().equals("")) {
            try {
                return Long.parseLong(timestampToleranceAsString);
            } catch (NumberFormatException ex) {
                throw new IllegalArgumentException("Could not parse provided value as long: " + timestampToleranceAsString);
            }
        } else {
            return null;
        }
    }
}
