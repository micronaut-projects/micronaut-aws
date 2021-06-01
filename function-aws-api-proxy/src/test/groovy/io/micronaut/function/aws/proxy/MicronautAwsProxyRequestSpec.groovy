package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.jaxrs.AwsProxySecurityContext
import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import spock.lang.Specification

class MicronautAwsProxyRequestSpec extends Specification {

    void "MicronautAwsProxyRequest::isSecurityContextPresent does not throw NPE"() {
        when:
        boolean isPresent = MicronautAwsProxyRequest.isSecurityContextPresent(null)

        then:
        noExceptionThrown()
        !isPresent

        when:
        isPresent = MicronautAwsProxyRequest.isSecurityContextPresent(new AwsProxySecurityContext(null, null))

        then:
        noExceptionThrown()
        !isPresent

        when:
        isPresent = MicronautAwsProxyRequest.isSecurityContextPresent(new AwsProxySecurityContext(null, new AwsProxyRequest()))

        then:
        noExceptionThrown()
        !isPresent
    }
}
