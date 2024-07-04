package io.micronaut.function.client.aws.v2;

import io.micronaut.function.client.FunctionClient;
import io.micronaut.http.annotation.Body;
import jakarta.inject.Named;

@FunctionClient
public interface TestFunctionClient {

    @Named("test-function")
    TestFunctionClientResponse invokeFunction(@Body TestFunctionClientRequest request);
}
