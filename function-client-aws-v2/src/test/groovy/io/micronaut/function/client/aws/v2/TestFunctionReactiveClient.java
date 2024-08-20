package io.micronaut.function.client.aws.v2;

import io.micronaut.function.client.FunctionClient;
import io.micronaut.http.annotation.Body;
import jakarta.inject.Named;
import org.reactivestreams.Publisher;

@FunctionClient
public interface TestFunctionReactiveClient {
    @Named("test-function-reactive")
    Publisher<TestFunctionClientResponse> invokeFunctionReactive(@Body TestFunctionClientRequest request);
}
