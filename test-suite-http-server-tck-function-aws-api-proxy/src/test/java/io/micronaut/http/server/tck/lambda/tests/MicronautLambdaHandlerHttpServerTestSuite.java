package io.micronaut.http.server.tck.lambda.tests;

import io.micronaut.http.server.tck.HttpServerTestSuite;
import org.junit.jupiter.api.Disabled;

import java.io.IOException;

public class MicronautLambdaHandlerHttpServerTestSuite implements HttpServerTestSuite {

    @Disabled
    @Override
    public void testRemoteAddressComesFromIdentitySourceIp() throws IOException {
        HttpServerTestSuite.super.testRemoteAddressComesFromIdentitySourceIp();
    }
}
