/*
    Copyright 2018 Amazon.com, Inc. or its affiliates. All Rights Reserved.

    Licensed under the Apache License, Version 2.0 (the "License"). You may not use this file
    except in compliance with the License. A copy of the License is located at

        http://aws.amazon.com/apache2.0/

    or in the "license" file accompanying this file. This file is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for
    the specific language governing permissions and limitations under the License.
 */

package io.micronaut.aws.alexa.httpserver;

/**
 * NOTICE: This class is forked from com.amazon.ask.servlet.ServletConstants https://github.com/alexa/alexa-skills-kit-sdk-for-java ask-sdk-servlet-support module
 *
 * Helper class to hold package's constant values.
 *
 * @author sdelamo
 * @since 2.0.0
 */
public final class AskHttpServerConstants {

    /**
     * The algorithm used to generate the signature.
     */
    public static final String SIGNATURE_ALGORITHM = "SHA1withRSA";

    /**
     * The character encoding used.
     */
    public static final String CHARACTER_ENCODING = "UTF-8";

    /**
     * The format of the certificate needed to verify the request signature.
     */
    public static final String SIGNATURE_CERTIFICATE_TYPE = "X.509";

    /**
     * The type of encryption key used to generate the signature.
     */
    public static final String SIGNATURE_TYPE = "RSA";

    /**
     * The domain name used by the Alexa Skills Kit API.
     */
    public static final String ECHO_API_DOMAIN_NAME = "echo-api.amazon.com";

    /**
     * The name of the request header that contains the signature.
     */
    public static final String SIGNATURE_REQUEST_HEADER = "Signature";

    /**
     * The name of the request header that contains the URL for the certificate chain needed to
     * verify the request signature.
     */
    public static final String SIGNATURE_CERTIFICATE_CHAIN_URL_REQUEST_HEADER =
            "SignatureCertChainUrl";

    /**
     * The name of the system property that can be used to disable request signature verification.
     * This feature verifies the certificate authenticity using the configured TrustStore and the
     * signature of the skill request, and will throw a {@link SecurityException} if the signature
     * does not pass verification. This feature should only be disabled in testing scenarios and
     * never in a production environment.
     */
    public static final String DISABLE_REQUEST_SIGNATURE_CHECK_SYSTEM_PROPERTY =
            "com.amazon.ask.servlet.disableRequestSignatureCheck";

    /**
     * The name of the system property that can be used to configure the timestamp tolerance (in
     * millis) of the {@link io.micronaut.aws.alexa.httpserver.verifiers.SkillRequestTimestampVerifier}. Requests with timestamps outside of this inclusive tolerance range,
     * either in the past or future, are rejected. If this property is not provided the default value,
     * {@value DEFAULT_TOLERANCE_MILLIS}, will be used.
     */
    public static final String TIMESTAMP_TOLERANCE_SYSTEM_PROPERTY =
            "com.amazon.speech.speechlet.servlet.timestampTolerance";

    /**
     * Default timestamp offset tolerance value in millis, used if a custom value is not provided through the
     * {@value TIMESTAMP_TOLERANCE_SYSTEM_PROPERTY} system property.
     */
    public static final long DEFAULT_TOLERANCE_MILLIS = 30000;

    /**
     * Maximum allowed timestamp offset tolerance value in millis.
     */
    public static final long MAXIMUM_TOLERANCE_MILLIS = 3600000;

    /**
     * Prevent instantiation.
     */
    private AskHttpServerConstants() { }
}
