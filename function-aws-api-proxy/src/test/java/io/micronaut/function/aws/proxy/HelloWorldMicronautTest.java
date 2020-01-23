/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.function.aws.proxy;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;

import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.cookie.Cookie;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import javax.ws.rs.core.HttpHeaders;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;

import static org.junit.Assert.*;


@RunWith(Parameterized.class)
public class HelloWorldMicronautTest {
    private static final String CUSTOM_HEADER_KEY = "X-Custom-Header";
    private static final String CUSTOM_HEADER_VALUE = "My Header Value";
    private static final String BODY_TEXT_RESPONSE = "Hello World";

    private static final String COOKIE_NAME = "MyCookie";
    private static final String COOKIE_VALUE = "CookieValue";
    private static final String COOKIE_DOMAIN = "mydomain.com";
    private static final String COOKIE_PATH = "/";

    private static MicronautLambdaContainerHandler handler;

    private boolean isAlb;

    public HelloWorldMicronautTest(boolean alb) {
        isAlb = alb;
    }

    @Parameterized.Parameters
    public static Collection<Object> data() {
        return Arrays.asList(new Object[] { false, true });
    }

    private AwsProxyRequestBuilder getRequestBuilder() {
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder();
        if (isAlb) builder.alb();

        return builder;
    }

    @BeforeClass
    public static void initializeServer() throws ContainerInitializationException {
        try {
            handler = new MicronautLambdaContainerHandler();
        } catch (RuntimeException e) {
            e.printStackTrace();
            fail();
        }
    }

    @AfterClass
    public static void stopMicronaut() throws IOException {
        handler.close();
    }

    @Test
    public void basicServer_handleRequest_emptyFilters() {
        AwsProxyRequest req = getRequestBuilder().method("GET").path("/hello").build();
        AwsProxyResponse response = handler.proxy(req, new MockLambdaContext());

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getMultiValueHeaders().containsKey(CUSTOM_HEADER_KEY));
        assertEquals(CUSTOM_HEADER_VALUE, response.getMultiValueHeaders().getFirst(CUSTOM_HEADER_KEY));
        assertEquals(BODY_TEXT_RESPONSE, response.getBody());
    }

    @Test
    public void addCookie_setCookieOnResponse_validCustomCookie() {
        AwsProxyRequest req = getRequestBuilder().method("GET").path("/cookie").build();
        AwsProxyResponse response = handler.proxy(req, new MockLambdaContext());

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getMultiValueHeaders().containsKey(HttpHeaders.SET_COOKIE));
        assertTrue(response.getMultiValueHeaders().getFirst(HttpHeaders.SET_COOKIE).contains(COOKIE_NAME + "=" + COOKIE_VALUE));
        assertTrue(response.getMultiValueHeaders().getFirst(HttpHeaders.SET_COOKIE).contains(COOKIE_DOMAIN));
        assertTrue(response.getMultiValueHeaders().getFirst(HttpHeaders.SET_COOKIE).contains(COOKIE_PATH));
    }

    @Test
    public void multiCookie_setCookieOnResponse_singleHeaderWithMultipleValues() {
        AwsProxyRequest req = getRequestBuilder().method("GET").path("/multi-cookie").build();
        AwsProxyResponse response = handler.proxy(req, new MockLambdaContext());

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getMultiValueHeaders().containsKey(HttpHeaders.SET_COOKIE));

        assertEquals(2, response.getMultiValueHeaders().get(HttpHeaders.SET_COOKIE).size());
        assertTrue(response.getMultiValueHeaders().getFirst(HttpHeaders.SET_COOKIE).contains(COOKIE_NAME + "=" + COOKIE_VALUE));
        assertTrue(response.getMultiValueHeaders().get(HttpHeaders.SET_COOKIE).get(1).contains(COOKIE_NAME + "2=" + COOKIE_VALUE + "2"));
        assertTrue(response.getMultiValueHeaders().getFirst(HttpHeaders.SET_COOKIE).contains(COOKIE_DOMAIN));
        assertTrue(response.getMultiValueHeaders().getFirst(HttpHeaders.SET_COOKIE).contains(COOKIE_PATH));
    }

    @Test
    public void rootResource_basicRequest_expectSuccess() {
        AwsProxyRequest req = getRequestBuilder().method("GET").path("/").build();
        AwsProxyResponse response = handler.proxy(req, new MockLambdaContext());

        assertEquals(200, response.getStatusCode());
        assertTrue(response.getMultiValueHeaders().containsKey(CUSTOM_HEADER_KEY));
        assertEquals(CUSTOM_HEADER_VALUE, response.getMultiValueHeaders().getFirst(CUSTOM_HEADER_KEY));
        assertEquals(BODY_TEXT_RESPONSE, response.getBody());
    }

    @Controller("/")
    public static class HelloController {
        @Get("/")
        HttpResponse<String> index() {
            return HttpResponse.ok(BODY_TEXT_RESPONSE)
                        .header(CUSTOM_HEADER_KEY, CUSTOM_HEADER_VALUE);
        }

        @Get("/hello")
        HttpResponse<String> hello() {
            return HttpResponse.ok(BODY_TEXT_RESPONSE)
                    .header(CUSTOM_HEADER_KEY, CUSTOM_HEADER_VALUE);
        }

        @Get("/cookie")
        HttpResponse<String> cookie() {
            final MutableHttpResponse<String> response = HttpResponse.ok(BODY_TEXT_RESPONSE);
            response.cookie(Cookie.of(COOKIE_NAME, COOKIE_VALUE).domain(COOKIE_DOMAIN).path(COOKIE_PATH));
            return response;
        }

        @Get("/multi-cookie")
        HttpResponse<String> multiCookie() {
            final MutableHttpResponse<String> response = HttpResponse.ok(BODY_TEXT_RESPONSE);
            response.cookie(Cookie.of(COOKIE_NAME, COOKIE_VALUE).domain(COOKIE_DOMAIN).path(COOKIE_PATH))
                    .cookie(Cookie.of(COOKIE_NAME + "2", COOKIE_VALUE + "2").domain(COOKIE_DOMAIN).path(COOKIE_PATH));
            return response;
        }
    }
}
