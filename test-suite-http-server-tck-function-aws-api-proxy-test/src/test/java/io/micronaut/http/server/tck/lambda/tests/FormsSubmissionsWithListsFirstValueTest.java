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
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.tck.AssertionUtils;
import io.micronaut.http.tck.HttpResponseAssertion;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

import static io.micronaut.http.tck.TestScenario.asserts;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Replaces the TCK's {@code FormsSubmissionsWithListsTest}, which expects every value of a repeated form key
 * ({@code usersId=1&usersId=2}). Micronaut's own server keeps all of those values only when binding a form body to an
 * object, but keeps just the first value when binding a single parameter, such as a {@code List<String>}. The
 * Lambda handler cannot tell the two apart, so it keeps the first value, matching the single parameter case.
 */
@SuppressWarnings({
    "java:S5960", // We're allowed assertions, as these are used in tests only
    "checkstyle:MissingJavadocType",
    "checkstyle:DesignForExtension"
})
class FormsSubmissionsWithListsFirstValueTest {

    private static final String SPEC_NAME = "FormsSubmissionsWithListsFirstValueTest";

    @Test
    void formWithListOfOneItem() throws IOException {
        assertWithBody(
            "question=en+que+trabajas&usersId=1",
            "{\"question\":\"en que trabajas\",\"usersId\":[1]}");
    }

    @Test
    void formWithListOfMoreThanOneKeepsOnlyTheFirstItem() throws IOException {
        assertWithBody(
            "question=en+que+trabajas&usersId=1&usersId=2",
            "{\"question\":\"en que trabajas\",\"usersId\":[1]}");
    }

    private static void assertWithBody(String body, String expected) throws IOException {
        asserts(SPEC_NAME,
            HttpRequest.POST("/questions/save", body).contentType(MediaType.APPLICATION_FORM_URLENCODED_TYPE),
            (server, request) -> AssertionUtils.assertDoesNotThrow(server, request, HttpResponseAssertion.builder()
                .status(HttpStatus.OK)
                .assertResponse(response -> {
                    Optional<String> responseBody = response.getBody(String.class);
                    assertTrue(responseBody.isPresent());
                    assertEquals(expected, responseBody.get());
                })
                .build()));
    }

    @Requires(property = "spec.name", value = SPEC_NAME)
    @Controller("/questions")
    static class QuestionController {

        @Consumes(MediaType.APPLICATION_FORM_URLENCODED)
        @Post("/save")
        HttpResponse<QuestionSave> save(@Body QuestionSave questionSave) {
            return HttpResponse.ok(questionSave);
        }
    }

    @Introspected
    record QuestionSave(String question, List<Long> usersId) {
    }
}
