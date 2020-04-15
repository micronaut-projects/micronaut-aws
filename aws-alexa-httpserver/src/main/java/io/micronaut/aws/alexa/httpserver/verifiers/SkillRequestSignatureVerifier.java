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

import io.micronaut.aws.alexa.conf.AlexaSkillConfigurationProperties;
import io.micronaut.aws.alexa.httpserver.AskHttpServerConstants;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;

import javax.inject.Singleton;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;
import java.io.Closeable;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.Proxy;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.security.KeyStore;
import java.security.Signature;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NOTICE: This class is forked from https://github.com/alexa/alexa-skills-kit-sdk-for-java ask-sdk-servlet-support module
 *
 * Provides a utility method to verify the signature of a skill request.
 *
 * @author sdelamo
 * @since 2.0.0
 */
@Requires(property = AlexaSkillConfigurationProperties.PREFIX + ".verifiers.signature", notEquals = StringUtils.FALSE)
@Requires(condition = RequestSignatureCheckSystemPropertyCondition.class)
@Singleton
public class SkillRequestSignatureVerifier implements SkillServletVerifier {
    /**
     * Map which serves as a cache to store public key certificates.
     */
    private static final Map<String, X509Certificate> CERTIFICATE_CACHE = new ConcurrentHashMap<>();

    /**
     * Used to check if the entry is for a domain name.
     */
    private static final Integer DOMAIN_NAME_SUBJECT_ALTERNATIVE_NAME_ENTRY = 2;

    /**
     * Certificate chain protocol.
     */
    private static final String VALID_SIGNING_CERT_CHAIN_PROTOCOL = "https";

    /**
     * Certificate chain host name.
     */
    private static final String VALID_SIGNING_CERT_CHAIN_URL_HOST_NAME = "s3.amazonaws.com";

    /**
     * Certificate chain url path prefix.
     */
    private static final String VALID_SIGNING_CERT_CHAIN_URL_PATH_PREFIX = "/echo.api/";

    /**
     * Used to validate the port to make the connection on.
     */
    private static final int UNSPECIFIED_SIGNING_CERT_CHAIN_URL_PORT_VALUE = -1;

    /**
     * Maximum number of trials to retrieve a certificate.
     */
    private static final int CERT_RETRIEVAL_RETRY_COUNT = 5;

    /**
     * Delay between each retry in milliseconds.
     */
    private static final int DELAY_BETWEEN_RETRIES_MS = 500;

    /**
     * Http OK response code.
     */
    private static final int HTTP_OK_RESPONSE_CODE = 200;

    /**
     * Represents a proxy setting, typically a type (http, socks) and a socket address.
     */
    private final Proxy proxy;

    /**
     * Constructor to build an instance of SkillRequestSignatureVerifier.
     */
    public SkillRequestSignatureVerifier() {
        this.proxy = null;
    }

    /**
     * @param proxy proxy configuration for certificate retrieval
     */
    public SkillRequestSignatureVerifier(final Proxy proxy) {
        this.proxy = proxy;
    }


    /**
     *
     * @param url URL where the certificate can be found.
     * @return public key certificate
     */
    public X509Certificate getCertificateFromCache(String url) {
        return CERTIFICATE_CACHE.get(url);
    }

    /**
     * Verifies the certificate authenticity using the configured TrustStore and the signature of
     * the skill request. This method will throw a {@link SecurityException} if the signature
     * does not pass verification.
     *
     * {@inheritDoc}
     */
    public void verify(final AlexaHttpRequest alexaHttpRequest) {
        String baseEncoded64Signature = alexaHttpRequest.getBaseEncoded64Signature();
        String signingCertificateChainUrl = alexaHttpRequest.getSigningCertificateChainUrl();
        if ((baseEncoded64Signature == null) || (signingCertificateChainUrl == null)) {
            throw new SecurityException(
                    "Missing signature/certificate for the provided skill request");
        }

        try {
            X509Certificate signingCertificate = getCertificateFromCache(signingCertificateChainUrl);
            if (signingCertificate != null && signingCertificate.getNotAfter().after(new Date())) {
                /*
                 * check the before/after dates on the certificate are still valid for the present
                 * time
                 */
                signingCertificate.checkValidity();
            } else {
                signingCertificate = retrieveAndVerifyCertificateChain(signingCertificateChainUrl);

                // if certificate is valid, then add it to the cache
                CERTIFICATE_CACHE.put(signingCertificateChainUrl, signingCertificate);
            }

            // verify that the request was signed by the provided certificate
            Signature signature = Signature.getInstance(AskHttpServerConstants.SIGNATURE_ALGORITHM);
            signature.initVerify(signingCertificate.getPublicKey());
            signature.update(alexaHttpRequest.getSerializedRequestEnvelope());

            if (!signature.verify(Base64.getDecoder().decode(baseEncoded64Signature
                    .getBytes(AskHttpServerConstants.CHARACTER_ENCODING)))) {
                throw new SecurityException(
                        "Failed to verify the signature/certificate for the provided skill request");
            }
        } catch (GeneralSecurityException | IOException ex) {
            throw new SecurityException(
                    "Failed to verify the signature/certificate for the provided skill request",
                    ex);
        }
    }

    /**
     * Retrieves the certificate from the specified URL and confirms that the certificate is valid.
     *
     * @param signingCertificateChainUrl
     *            the URL to retrieve the certificate chain from
     * @return the certificate at the specified URL, if the certificate is valid
     * @throws CertificateException
     *             if the certificate cannot be retrieve or is invalid
     */
    private X509Certificate retrieveAndVerifyCertificateChain(final String signingCertificateChainUrl) throws CertificateException {
        for (int attempt = 0; attempt <= CERT_RETRIEVAL_RETRY_COUNT; attempt++) {
            InputStream in = null;
            try {
                HttpURLConnection connection =
                        proxy != null ? (HttpURLConnection) getAndVerifySigningCertificateChainUrl(signingCertificateChainUrl).openConnection(proxy)
                                : (HttpURLConnection) getAndVerifySigningCertificateChainUrl(signingCertificateChainUrl).openConnection();

                if (connection.getResponseCode() != HTTP_OK_RESPONSE_CODE) {
                    if (waitForRetry(attempt)) {
                        continue;
                    } else {
                        throw new CertificateException("Got a non-200 status code when retrieving certificate at URL: " + signingCertificateChainUrl);
                    }
                }

                in = connection.getInputStream();
                CertificateFactory certificateFactory =
                        CertificateFactory.getInstance(AskHttpServerConstants.SIGNATURE_CERTIFICATE_TYPE);
                @SuppressWarnings("unchecked")
                Collection<X509Certificate> certificateChain =
                        (Collection<X509Certificate>) certificateFactory.generateCertificates(in);
                /*
                 * check the before/after dates on the certificate date to confirm that it is valid on
                 * the current date
                 */
                X509Certificate signingCertificate = certificateChain.iterator().next();
                signingCertificate.checkValidity();

                // check the certificate chain
                TrustManagerFactory trustManagerFactory =
                        TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
                trustManagerFactory.init((KeyStore) null);

                X509TrustManager x509TrustManager = null;
                for (TrustManager trustManager : trustManagerFactory.getTrustManagers()) {
                    if (trustManager instanceof X509TrustManager) {
                        x509TrustManager = (X509TrustManager) trustManager;
                    }
                }

                if (x509TrustManager == null) {
                    throw new IllegalStateException(
                            "No X509 TrustManager available. Unable to check certificate chain");
                } else {
                    x509TrustManager.checkServerTrusted(
                            certificateChain.toArray(new X509Certificate[certificateChain.size()]),
                            AskHttpServerConstants.SIGNATURE_TYPE);
                }

                /*
                 * verify Echo API's hostname is specified as one of subject alternative names on the
                 * signing certificate
                 */
                if (!subjectAlernativeNameListContainsEchoSdkDomainName(signingCertificate
                        .getSubjectAlternativeNames())) {
                    throw new CertificateException(
                            "The provided certificate is not valid for the ASK SDK");
                }

                return signingCertificate;
            } catch (IOException e) {
                if (!waitForRetry(attempt)) {
                    throw new CertificateException("Unable to retrieve certificate from URL: " + signingCertificateChainUrl, e);
                }
            } catch (Exception e) {
                throw new CertificateException("Unable to verify certificate at URL: " + signingCertificateChainUrl, e);
            } finally {
                if (in != null) {
                    closeQuietly(in);
                }
            }
        }
        throw new RuntimeException("Unable to retrieve signing certificate due to an unhandled exception");
    }

    /**
     * Close and eats exception if any.
     * @param closeable Closeable object.
     */
    public static void closeQuietly(Closeable closeable) {
        try {
            if (closeable != null) {
                closeable.close();
            }
        } catch (IOException var2) {
        }

    }

    /**
     * Checks if the system should retry to fetch a certificate.
     * @param attempt Nth attempt.
     * @return true if the number of attempts to retrieve certificates does not exceed pre-mentioned value.
     */
    private boolean waitForRetry(final int attempt) {
        if (attempt < CERT_RETRIEVAL_RETRY_COUNT) {
            try {
                Thread.sleep(DELAY_BETWEEN_RETRIES_MS);
                return true;
            } catch (InterruptedException ex) {
                throw new RuntimeException("Interrupted while waiting for certificate retrieval retry attempt", ex);
            }
        } else {
            return false;
        }
    }

    /**
     * Verify Echo API's hostname is specified as one of subject alternative names on the signing certificate.
     * @param subjectAlternativeNameEntries name entries.
     * @return true if subject alternative entry is in the expected form and if the entry is for a domain name and that domain name
     * matches the domain name for the echo sdk.
     */
    private boolean subjectAlernativeNameListContainsEchoSdkDomainName(
            final Collection<List<?>> subjectAlternativeNameEntries) {
        for (List<?> entry : subjectAlternativeNameEntries) {
            // first ensure that the subject alternative entry is in the expected form
            if (entry.get(0) instanceof Integer && entry.get(1) instanceof String) {
                /*
                 * if the entry is for a domain name and that domain name matches the domain name
                 * for the echo sdk then return true
                 */
                if (DOMAIN_NAME_SUBJECT_ALTERNATIVE_NAME_ENTRY.equals(entry.get(0))
                        && AskHttpServerConstants.ECHO_API_DOMAIN_NAME.equals((entry.get(1)))) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Verifies the signing certificate chain URL and returns a {@code URL} object.
     *
     * @param signingCertificateChainUrl
     *            the external form of the URL
     * @return the URL
     * @throws CertificateException
     *             if the URL is malformed or contains an invalid hostname, an unsupported protocol,
     *             or an invalid port (if specified)
     */
    static URL getAndVerifySigningCertificateChainUrl(final String signingCertificateChainUrl)
            throws CertificateException {
        try {
            URL url = new URI(signingCertificateChainUrl).normalize().toURL();
            // Validate the hostname
            if (!VALID_SIGNING_CERT_CHAIN_URL_HOST_NAME.equalsIgnoreCase(url.getHost())) {
                throw new CertificateException(String.format(
                        "SigningCertificateChainUrl [%s] does not contain the required hostname"
                                + " of [%s]", signingCertificateChainUrl,
                        VALID_SIGNING_CERT_CHAIN_URL_HOST_NAME));
            }

            // Validate the path prefix
            String path = url.getPath();
            if (!path.startsWith(VALID_SIGNING_CERT_CHAIN_URL_PATH_PREFIX)) {
                throw new CertificateException(String.format(
                        "SigningCertificateChainUrl path [%s] is invalid. Expecting path to "
                                + "start with [%s]", signingCertificateChainUrl,
                        VALID_SIGNING_CERT_CHAIN_URL_PATH_PREFIX));
            }

            // Validate the protocol
            String urlProtocol = url.getProtocol();
            if (!VALID_SIGNING_CERT_CHAIN_PROTOCOL.equalsIgnoreCase(urlProtocol)) {
                throw new CertificateException(String.format(
                        "SigningCertificateChainUrl [%s] contains an unsupported protocol [%s]",
                        signingCertificateChainUrl, urlProtocol));
            }

            // Validate the port uses the default of 443 for HTTPS if explicitly defined in the URL
            int urlPort = url.getPort();
            if ((urlPort != UNSPECIFIED_SIGNING_CERT_CHAIN_URL_PORT_VALUE)
                    && (urlPort != url.getDefaultPort())) {
                throw new CertificateException(String.format(
                        "SigningCertificateChainUrl [%s] contains an invalid port [%d]",
                        signingCertificateChainUrl, urlPort));
            }

            return url;
        } catch (IllegalArgumentException | MalformedURLException | URISyntaxException ex) {
            throw new CertificateException(String.format(
                    "SigningCertificateChainUrl [%s] is malformed", signingCertificateChainUrl), ex);
        }
    }

}
