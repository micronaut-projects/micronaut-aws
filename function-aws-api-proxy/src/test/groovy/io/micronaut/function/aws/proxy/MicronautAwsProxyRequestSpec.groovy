package io.micronaut.function.aws.proxy

import com.amazonaws.serverless.proxy.internal.jaxrs.AwsProxySecurityContext
import com.amazonaws.serverless.proxy.model.AwsProxyRequest
import com.amazonaws.serverless.proxy.model.ContainerConfig
import com.amazonaws.serverless.proxy.model.Headers
import com.amazonaws.serverless.proxy.model.SingleValueHeaders
import com.amazonaws.services.lambda.runtime.Context
import io.micronaut.core.convert.ConversionService
import spock.lang.Specification

import javax.ws.rs.core.SecurityContext

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

    void multiAndSingleValueHeadersShouldBeMerged() {
        given:
        String headerName = "tenant"
        String headerValue = "my-tenant"
        AwsProxyRequest awsProxyRequest = new AwsProxyRequest();
        awsProxyRequest.setHeaders(new SingleValueHeaders());
        awsProxyRequest.getHeaders().put(headerName, headerValue);
        awsProxyRequest.setMultiValueHeaders(new Headers());
        awsProxyRequest.getMultiValueHeaders().put(headerName, Collections.singletonList(headerValue));

        when:
        MicronautAwsProxyRequest<?> result = new MicronautAwsProxyRequest<>("/",
                awsProxyRequest, Mock(SecurityContext), Mock(Context), Mock(ContainerConfig), Mock(ConversionService));

        then:
        Arrays.asList(headerValue) == result.getHeaders().getAll(headerName)
    }
}
