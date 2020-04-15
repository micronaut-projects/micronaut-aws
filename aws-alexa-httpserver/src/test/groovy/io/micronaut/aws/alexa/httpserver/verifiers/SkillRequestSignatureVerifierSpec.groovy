package io.micronaut.aws.alexa.httpserver.verifiers

import com.amazon.ask.model.RequestEnvelope
import io.micronaut.aws.alexa.httpserver.AskHttpServerConstants
import io.micronaut.core.convert.ConversionService
import io.micronaut.http.MutableHttpHeaders
import io.micronaut.http.simple.SimpleHttpHeaders
import org.bouncycastle.jce.provider.BouncyCastleProvider
import org.bouncycastle.x509.X509V3CertificateGenerator
import org.junit.Test
import spock.lang.Specification

import javax.security.auth.x500.X500Principal
import java.security.KeyPair
import java.security.KeyPairGenerator
import java.security.NoSuchAlgorithmException
import java.security.PrivateKey
import java.security.Signature
import java.security.cert.CertificateException
import java.security.cert.X509Certificate

import static io.micronaut.aws.alexa.httpserver.verifiers.SkillRequestSignatureVerifier.getAndVerifySigningCertificateChainUrl
import static java.security.Security.addProvider

/**
 * NOTICE: This test is a spock rewrite of com.amazon.ask.servlet.verifiers.SkillRequestSignatureVerifierTest https://github.com/alexa/alexa-skills-kit-sdk-for-java ask-sdk-servlet-support module
 */
class SkillRequestSignatureVerifierSpec extends Specification {
    private static final String PREPOPULATED_CERT_URL =
            "https://s3.amazonaws.com/echo.api/doesnotexist";
    private static final String VALID_URL = "https://s3.amazonaws.com/echo.api/cert";
    private static final String VALID_URL_WITH_PORT = "https://s3.amazonaws.com:443/echo.api/cert";
    private static final String VALID_URL_WITH_PATH_TRAVERSAL =
            "https://s3.amazonaws.com/echo.api/../echo.api/cert";
    private static final String INVALID_URL_WITH_INVALID_HOST_NAME =
            "https://very.bad/echo.api/cert";
    private static final String INVALID_URL_WITH_UNSUPPORTED_PROTOCOL =
            "http://s3.amazonaws.com/echo.api/cert";
    private static final String INVALID_URL_WITH_INVALID_PORT =
            "https://s3.amazonaws.com:563/echo.api/cert";
    private static final String INVALID_URL_WITH_INVALID_PATH = "https://s3.amazonaws.com/cert";
    private static final String INVALID_URL_WITH_INVALID_PATH_TRAVERSAL =
            "https://s3.amazonaws.com/echo.api/../cert";
    private static final String INVALID_URL_WITH_INVALID_UPPER_CASE_PATH =
            "https://s3.amazonaws.com/ECHO.API/cert";
    private static final String MALFORMED_URL = "badUrl";

    private static RequestEnvelope deserializedRequestEnvelope = RequestEnvelope.builder().build()
    private static SkillRequestSignatureVerifier verifier = new SkillRequestSignatureVerifier()

    def setupSpec() {
        addProvider(new BouncyCastleProvider())
    }

    void "getAndVerifySigningCertificateChainUrl validUrlValue urlReturned"() {
        expect:
        VALID_URL == getAndVerifySigningCertificateChainUrl(VALID_URL).toExternalForm()
    }

    void "getAndVerifySigningCertificateChainUrl_validUrlWithPort_noExceptionThrown"() {
        expect:
        VALID_URL_WITH_PORT == getAndVerifySigningCertificateChainUrl(VALID_URL_WITH_PORT).toExternalForm()
    }

    void "getAndVerifySigningCertificateChainUrl_validUrlWithPathTraversal_noExceptionThrown"() {
        expect:
        VALID_URL == getAndVerifySigningCertificateChainUrl(VALID_URL_WITH_PATH_TRAVERSAL).toExternalForm()
    }

    void "getAndVerifySigningCertificateChainUrl invalidHostnameInUrl certificateExceptionThrown"() {
        when:
        getAndVerifySigningCertificateChainUrl(INVALID_URL_WITH_INVALID_HOST_NAME)

        then:
        CertificateException e = thrown()
        e.message.contains("does not contain the required hostname")
    }

    void "getAndVerifySigningCertificateChainUrl_unsupportedProtocolInUrl_certificateExceptionThrown"() {
        when:
        getAndVerifySigningCertificateChainUrl(INVALID_URL_WITH_UNSUPPORTED_PROTOCOL)

        then:
        CertificateException e = thrown()
        e.message.contains("contains an unsupported protocol")
    }

    void "getAndVerifySigningCertificateChainUrl invalidPortInUrl certificateExceptionThrown"() {
        when:
        getAndVerifySigningCertificateChainUrl(INVALID_URL_WITH_INVALID_PORT)

        then:
        CertificateException e = thrown()
        e.message.contains("contains an invalid port")
    }

    @Test
    void "getAndVerifySigningCertificateChainUrl malformedUrl certificateExceptionThrown"() {
        when:
        getAndVerifySigningCertificateChainUrl(MALFORMED_URL)

        then:
        CertificateException e = thrown()
        //e instanceof IllegalArgumentException
        e.message.contains("is malformed")
    }

    void "getAndVerifySigningCertificateChainUrl_invalidPath_certificateExceptionThrown"() {
        when:
        getAndVerifySigningCertificateChainUrl(INVALID_URL_WITH_INVALID_PATH)

        then:
        CertificateException e = thrown()
        e.message.contains("Expecting path to start with")
    }

    void "getAndVerifySigningCertificateChainUrl invalidPathTraversal certificateExceptionThrown"() {
        when:
        getAndVerifySigningCertificateChainUrl(INVALID_URL_WITH_INVALID_PATH_TRAVERSAL)

        then:
        CertificateException e = thrown()
        e.message.contains("Expecting path to start with")
    }

    void "getAndVerifySigningCertificateChainUrl invalidUpperCasePath certificateExceptionThrown"() {
        when:
        getAndVerifySigningCertificateChainUrl(INVALID_URL_WITH_INVALID_UPPER_CASE_PATH)

        then:
        CertificateException e = thrown()
        e.message.contains("Expecting path to start with")
    }

    void "checkRequestSignature validSignatureValidPrivateKey noSecurityExceptionThrown"() {
        given:
        KeyPair keyPair = generateKeyPair()
        PrivateKey validPrivateKey = keyPair.getPrivate()
        String testContent = "This is some test content."
        byte[] signature = signContent(testContent, validPrivateKey)
        MutableHttpHeaders headers = new SimpleHttpHeaders([
                (AskHttpServerConstants.SIGNATURE_REQUEST_HEADER): Base64.encoder.encodeToString(signature),
                (AskHttpServerConstants.SIGNATURE_CERTIFICATE_CHAIN_URL_REQUEST_HEADER): PREPOPULATED_CERT_URL
        ], ConversionService.SHARED)
        SkillServletVerifier verifier = new CustomSkillRequestSignatureVerifier(keyPair)

        when:
        verifier.verify(new HttpServerAlexaHttpRequest(headers, testContent.getBytes(), deserializedRequestEnvelope))

        then:
        noExceptionThrown()
    }

    static class CustomSkillRequestSignatureVerifier extends SkillRequestSignatureVerifier {
        private X509Certificate cert;
        CustomSkillRequestSignatureVerifier(KeyPair keyPair) {
            this.cert = certWithKeyPair(keyPair)
        }
        @Override
        X509Certificate getCertificateFromCache(String url) {
            return cert;
        }

    }

    private static X509Certificate certWithKeyPair(KeyPair keyPair) {
        X509V3CertificateGenerator certGen = new X509V3CertificateGenerator()
        certGen.setSerialNumber(BigInteger.valueOf(System.currentTimeMillis()))
        X500Principal self = new X500Principal("CN=Test Certificate")
        certGen.setIssuerDN(self)
        certGen.setSubjectDN(self)
        certGen.setNotBefore(new Date(System.currentTimeMillis() - 60000))
        certGen.setNotAfter(new Date(System.currentTimeMillis() + 60000))
        certGen.setPublicKey(keyPair.getPublic())
        certGen.setSignatureAlgorithm(AskHttpServerConstants.SIGNATURE_ALGORITHM)
        // BC means the Bouncy Castle security provider.
        X509Certificate cert = certGen.generate(keyPair.getPrivate(), "BC")
        cert
    }

    void "checkRequestSignature validSignatureInvalidPrivateKey securityExceptionThrown"() {
        given:
        String testContent = "This is some test content."
        byte[] signature = signContent(testContent, generateKeyPair().getPrivate())
        MutableHttpHeaders headers = new SimpleHttpHeaders([
                (AskHttpServerConstants.SIGNATURE_REQUEST_HEADER): Base64.encoder.encodeToString(signature),
                (AskHttpServerConstants.SIGNATURE_CERTIFICATE_CHAIN_URL_REQUEST_HEADER): PREPOPULATED_CERT_URL
        ], ConversionService.SHARED)

        when:
        verifier.verify(new HttpServerAlexaHttpRequest(headers, testContent.getBytes(), deserializedRequestEnvelope));

        then:
        SecurityException e = thrown()
        e.message.contains("Failed to verify the signature/certificate for the provided skill request")
    }

    private static KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(AskHttpServerConstants.SIGNATURE_TYPE)
        keyPairGenerator.initialize(512)
        keyPairGenerator.generateKeyPair()
    }

    private static byte[] signContent(String content, PrivateKey key) throws Exception {
        Signature signature = Signature.getInstance(AskHttpServerConstants.SIGNATURE_ALGORITHM)
        signature.initSign(key)
        signature.update(content.getBytes())
        signature.sign()
    }
}
