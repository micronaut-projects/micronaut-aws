package io.micronaut.http.server.tck.lambda.tests;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.tck.AssertionUtils;
import io.micronaut.http.tck.BodyAssertion;
import io.micronaut.http.tck.HttpResponseAssertion;
import org.junit.jupiter.api.Test;
import java.io.IOException;

import static io.micronaut.http.tck.TestScenario.asserts;

public class SingleValueHeaderTest {
    private static final String SPEC_NAME = "SingleValueHeaderTest";

    @Test
    void noHeaderSent() throws IOException {
        asserts(SPEC_NAME,
                HttpRequest.GET("/single-value-header").accept(MediaType.TEXT_PLAIN),
                (server, request) -> AssertionUtils.assertDoesNotThrow(server, request, HttpResponseAssertion.builder()
                        .status(HttpStatus.OK)
                        .body(BodyAssertion.builder().body("HEADER NOT DEFINED").equals())
                        .build()));
    }

    @Test
    void headerSent() throws IOException {
        asserts(SPEC_NAME,
                HttpRequest.GET("/single-value-header").accept(MediaType.TEXT_PLAIN).header("sample-header", "sample-value"),
                (server, request) -> AssertionUtils.assertDoesNotThrow(server, request, HttpResponseAssertion.builder()
                        .status(HttpStatus.OK)
                        .body(BodyAssertion.builder().body("sample-value").equals())
                        .build()));
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @Controller
    static class SampleController {

        @Produces(MediaType.TEXT_PLAIN)
        @Get("/single-value-header")
        String singleValueHeader(HttpRequest<?> request) {
            return request.getHeaders().get("sample-header", String.class).orElse("HEADER NOT DEFINED");
        }
    }
}
