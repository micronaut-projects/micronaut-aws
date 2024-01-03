package io.micronaut.function.aws.runtime.micronautrequeststreamhandler;

import com.amazonaws.services.lambda.runtime.RequestStreamHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.core.annotation.Nullable;
import io.micronaut.function.aws.runtime.AbstractRequestStreamHandlerMicronautLambdaRuntime;
import io.micronaut.function.aws.runtime.ReservedRuntimeEnvironmentVariables;
import io.micronaut.logging.LogLevel;

class FunctionLambdaRuntime
        extends AbstractRequestStreamHandlerMicronautLambdaRuntime<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private final String serverUrl

    FunctionLambdaRuntime(String serverUrl) {
        this.serverUrl = serverUrl
    }

    @Override
    protected String getEnv(String name) {
        return name == ReservedRuntimeEnvironmentVariables.AWS_LAMBDA_RUNTIME_API ? serverUrl : super.getEnv(name)
    }

    @Override
    protected LogLevel getLogLevel() {
        return LogLevel.TRACE
    }

    @Override
    protected @Nullable
    RequestStreamHandler createRequestStreamHandler(String... args) {
        return new FunctionRequestHandler()
    }

}
