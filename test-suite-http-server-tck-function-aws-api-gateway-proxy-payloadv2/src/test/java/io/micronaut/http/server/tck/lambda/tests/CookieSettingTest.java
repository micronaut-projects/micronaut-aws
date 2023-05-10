package io.micronaut.http.server.tck.lambda.tests;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.tck.AssertionUtils;
import io.micronaut.http.tck.BodyAssertion;
import io.micronaut.http.tck.HttpResponseAssertion;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static io.micronaut.http.tck.TestScenario.asserts;

class CookieSettingTest {

    public static final String SPEC_NAME = "CookieSettingTest";

    @Test
    void setCookieIsReturned() throws IOException {
        asserts(SPEC_NAME,
            HttpRequest.GET("/cookie"),
            (server, request) -> AssertionUtils.assertDoesNotThrow(server, request, HttpResponseAssertion.builder()
                .status(HttpStatus.OK)
                .body(BodyAssertion.builder().body("Yay").equals())
                .header(HttpHeaders.SET_COOKIE, "test=value; Secure")
                .build()));
    }

    @Controller("/cookie")
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class CookieController {

        @Get
        HttpResponse<String> get() {
            return HttpResponse.ok("Yay")
                .cookie(Cookie.of("test", "value").secure(true));
        }
    }
}
