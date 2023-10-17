/*
 * Copyright 2017-2023 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.http.server.tck.lambda.tests;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.annotation.Introspected;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.tck.AssertionUtils;
import io.micronaut.http.tck.HttpResponseAssertion;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static io.micronaut.http.tck.TestScenario.asserts;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SuppressWarnings({
        "java:S5960", // We're allowed assertions, as these are used in tests only
        "checkstyle:MissingJavadocType",
        "checkstyle:DesignForExtension"
})
public class HeadersReplacementTest {
    public static final String SPEC_NAME = "HeadersReplacementTest";

    /**
     * Multiple Headers are properly received as list and not as single header
     */
    @Test
    void multipleHeadersAreReceivedAsList() throws IOException {
        asserts(SPEC_NAME,
                HttpRequest.GET("/foo/receive-multiple-headers")
                        .header(HttpHeaders.ETAG, "A")
                        .header(HttpHeaders.ETAG, "B"),
                (server, request) -> AssertionUtils.assertDoesNotThrow(server, request, HttpResponseAssertion.builder()
                        .status(HttpStatus.OK)
                        .assertResponse(response -> {
                            Optional<String> eTagsOptional = response.getBody(String.class);
                            assertTrue(eTagsOptional.isPresent());
                            String json = eTagsOptional.get();
                            assertTrue(
                                    json.equals("{\"headers\":[\"A\",\"B\"]}") ||
                                            json.equals("{\"headers\":[\"A,B\"]}")
                            );
                        })
                        .build()));
    }

    @Controller("/foo")
    @Requires(property = "spec.name", value = SPEC_NAME)
    static class ProduceController {
        @Get(value = "/receive-multiple-headers")
        ETags receiveMultipleHeaders(HttpRequest<?> request) {
            return new ETags(request.getHeaders().getAll(HttpHeaders.ETAG));
        }
    }

    @Introspected
    record ETags(List<String> headers) {
    }
}
