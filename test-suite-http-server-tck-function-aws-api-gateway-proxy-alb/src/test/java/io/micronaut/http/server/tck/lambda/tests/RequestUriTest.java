package io.micronaut.http.server.tck.lambda.tests;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.tck.AssertionUtils;
import io.micronaut.http.tck.HttpResponseAssertion;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Arrays;

import static io.micronaut.http.tck.TestScenario.asserts;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({
    "java:S5960", // We're allowed assertions, as these are used in tests only
    "checkstyle:MissingJavadocType",
    "checkstyle:DesignForExtension"
})
class RequestUriTest {
    private static final String SPEC_NAME = "RequestUriTest";

    @Test
    void fullUriIsProperlyInjected() throws IOException {
        String queryPart = "key1=value1&key1=value2&key2=value3";
        String fullUri = "/uri/fullUri?" + queryPart;
        asserts(SPEC_NAME,
            HttpRequest.GET(fullUri),
            (server, request) -> AssertionUtils.assertDoesNotThrow(server, request, HttpResponseAssertion.builder()
                .status(HttpStatus.OK)
                .assertResponse(response -> {
                    String uri = response.getBody().map(Object::toString).orElseThrow();

                    Arrays.stream(queryPart.split("&"))
                        .forEach(part -> assertTrue(uri.contains(part)));
                })
                .build()));
    }

    @Controller("/uri")
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class RequestUriController {
        @Get("/fullUri")
        @Produces(MediaType.TEXT_PLAIN)
        String uri(HttpRequest<?> request) {
            return request.getUri().toASCIIString();
        }
    }
}
