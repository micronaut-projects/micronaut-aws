package io.micronaut.function.aws.runtime

import io.micronaut.http.HttpRequest
import spock.lang.Shared
import spock.lang.Specification
import spock.lang.Subject

class AwsLambdaRuntimeApiSpec extends Specification {
    @Shared
    String requestId = "666e7e60-e583-483b-ae3a-2ed7c387823d"

    @Subject
    AwsLambdaRuntimeApi awslambdaRuntimeApi = new AwsLambdaRuntimeApi() {}

    void "verify invocation uri creation"() {
        expect:
        awslambdaRuntimeApi.responseUri(requestId) == "/2018-06-01/runtime/invocation/666e7e60-e583-483b-ae3a-2ed7c387823d/response"
    }

    void "verify error uri creation"() {
        expect:
        awslambdaRuntimeApi.errorUri(requestId) == "/2018-06-01/runtime/invocation/666e7e60-e583-483b-ae3a-2ed7c387823d/error"
    }

    void "initialization error request"() {
        given:
        String errorMessage = "Failed to load function."
        String errorType = "InvalidFunctionException"
        String lambdaFunctionErrorType = "Unhandled"

        when:
        HttpRequest request = awslambdaRuntimeApi.initializationErrorRequest(errorMessage, errorType, lambdaFunctionErrorType)

        then:
        request.getPath() == '/2018-06-01/runtime/init/error'

        request.getBody(AwsLambdaRuntimeApiError).isPresent()
        request.getBody(AwsLambdaRuntimeApiError).get().errorMessage == errorMessage
        request.getBody(AwsLambdaRuntimeApiError).get().errorType == errorType
        request.getHeaders().get('Lambda-Runtime-Function-Error-Type') == 'Unhandled'
    }

    void "invocation error request"() {
        given:
        String errorMessage = "Failed to load function."
        String errorType = "InvalidFunctionException"
        String lambdaFunctionErrorType = "Unhandled"

        when:
        HttpRequest request = awslambdaRuntimeApi.invocationErrorRequest(requestId, errorMessage, errorType, lambdaFunctionErrorType)

        then:
        request.getPath() == '/2018-06-01/runtime/invocation/666e7e60-e583-483b-ae3a-2ed7c387823d/error'

        request.getBody(AwsLambdaRuntimeApiError).isPresent()
        request.getBody(AwsLambdaRuntimeApiError).get().errorMessage == errorMessage
        request.getBody(AwsLambdaRuntimeApiError).get().errorType == errorType
        request.getHeaders().get('Lambda-Runtime-Function-Error-Type') == 'Unhandled'
    }

    void "invocation response request"() {
        given:
        String body = "SUCCESS"

        when:
        HttpRequest request = awslambdaRuntimeApi.invocationResponseRequest(requestId, body)

        then:
        request.getPath() == '/2018-06-01/runtime/invocation/666e7e60-e583-483b-ae3a-2ed7c387823d/response'

        request.getBody(String).isPresent()
        request.getBody(String).get() == "SUCCESS"
    }
}
