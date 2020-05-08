package io.micronaut.function.aws.runtime

import io.micronaut.docs.BookLambdaRuntime

class CustomAwsProxyEventMicronautLambdaRuntime extends BookLambdaRuntime {

    String serverUrl

    CustomAwsProxyEventMicronautLambdaRuntime(String serverUrl) {
        this.serverUrl = serverUrl
    }

    @Override
    String getEnv(String name) {
        if (name == ReservedRuntimeEnvironmentVariables.AWS_LAMBDA_RUNTIME_API) {
            return serverUrl
        } else if (name == ReservedRuntimeEnvironmentVariables.HANDLER) {
            return 'io.micronaut.docs.BookRequestHandler'
        }
        super.getEnv(name)
    }
}
