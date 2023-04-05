package io.micronaut.http.server.tck.lambda;

import io.micronaut.http.tck.ServerUnderTest;
import io.micronaut.http.tck.ServerUnderTestProvider;

import java.util.Map;

public class LambdaServerUnderTestProvider implements ServerUnderTestProvider {
    @Override
    public ServerUnderTest getServer(Map<String, Object> properties) {
        return new LambdaServerUnderTest(properties);
    }
}
