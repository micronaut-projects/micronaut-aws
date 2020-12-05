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
import com.amazonaws.serverless.proxy.internal.testutils.AwsProxyRequestBuilder;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.Context;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.util.CollectionUtils;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.MediaType;
import org.apache.commons.codec.binary.Base64;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Unit test class for the Jersey AWS_PROXY default implementation
 */
@RunWith(Parameterized.class)
public class MicronautAwsProxyTest {
    public static final String SERVLET_RESP_HEADER_KEY = "X-HttpServletResponse";
    private static final String CUSTOM_HEADER_KEY = "x-custom-header";
    private static final String CUSTOM_HEADER_VALUE = "my-custom-value";
    private static final String AUTHORIZER_PRINCIPAL_ID = "test-principal-" + UUID.randomUUID().toString();
    private static final String USER_PRINCIPAL = "user1";

    private static ObjectMapper objectMapper = new ObjectMapper();
    private static MicronautLambdaContainerHandler handler;
    private static Context lambdaContext = new MockLambdaContext();

    private boolean isAlb;

    public MicronautAwsProxyTest(boolean alb) {
        isAlb = alb;
    }

    @BeforeClass
    public static void initHandler() {
        try {
            handler = new MicronautLambdaContainerHandler(
                    ApplicationContext.build(CollectionUtils.mapOf(
                            "spec.name", "MicronautAwsProxyTest",
                            "micronaut.security.enabled", true,
                            "micronaut.views.handlebars.enabled", true,
                            "micronaut.router.static-resources.lorem.paths", "classpath:static-lorem/",
                            "micronaut.router.static-resources.lorem.mapping", "/static-lorem/**"
                    ))
            );
        } catch (ContainerInitializationException e) {
            throw new RuntimeException("Test failed to start: " + e.getMessage(), e);
        }
    }

    @AfterClass
    public static void cleanup() {
        handler.close();
    }

    @Parameterized.Parameters(name = "isALB == {0}")
    public static Collection<Object> data() {
        return Arrays.asList(new Object[] { false, true });
    }

    private AwsProxyRequestBuilder getRequestBuilder(String path, String method) {
        AwsProxyRequestBuilder builder = new AwsProxyRequestBuilder(path, method);
        if (isAlb) builder.alb();

        return builder;
    }

    @Test
    public void queryParam_listOfString_expectCorrectLength() {
        AwsProxyRequest request = getRequestBuilder("/echo/list-query-string", "GET").queryString("list", "v1,v2,v3").build();
        AwsProxyResponse resp = handler.proxy(request, lambdaContext);
        assertNotNull(resp);
        assertEquals(resp.getStatusCode(), 200);
        validateSingleValueModel(resp, "3");
    }


    @Test
    public void alb_basicRequest_expectSuccess() {
        AwsProxyRequest request = getRequestBuilder("/echo/headers", "GET")
                .json()
                .header(CUSTOM_HEADER_KEY, CUSTOM_HEADER_VALUE)
                .alb()
                .build();

        AwsProxyResponse output = handler.proxy(request, lambdaContext);
        assertEquals(200, output.getStatusCode());
        assertEquals("application/json", output.getMultiValueHeaders().getFirst("Content-Type"));
        assertNotNull(output.getStatusDescription());
        System.out.println(output.getStatusDescription());

        validateMapResponseModel(output);
    }

    @Test
    public void headers_getHeaders_echo() {
        AwsProxyRequest request = getRequestBuilder("/echo/headers", "GET")
                .json()
                .header(CUSTOM_HEADER_KEY, CUSTOM_HEADER_VALUE)
                .build();

        AwsProxyResponse output = handler.proxy(request, lambdaContext);
        assertEquals(200, output.getStatusCode());
        assertEquals("application/json", output.getMultiValueHeaders().getFirst("Content-Type"));

        validateMapResponseModel(output);
    }

    @Test
    public void headers_servletRequest_echo() {
        AwsProxyRequest request = getRequestBuilder("/echo/servlet-headers", "GET")
                .json()
                .header(CUSTOM_HEADER_KEY, CUSTOM_HEADER_VALUE)
                .build();

        AwsProxyResponse output = handler.proxy(request, lambdaContext);
        assertEquals(200, output.getStatusCode());
        assertEquals("application/json", output.getMultiValueHeaders().getFirst("Content-Type"));

        validateMapResponseModel(output);
    }

    @Test
    public void context_servletResponse_setCustomHeader() {
        AwsProxyRequest request = getRequestBuilder("/echo/servlet-response", "GET")
                .json()
                .build();

        AwsProxyResponse output = handler.proxy(request, lambdaContext);
        assertEquals(200, output.getStatusCode());
        assertTrue(output.getMultiValueHeaders().containsKey(SERVLET_RESP_HEADER_KEY));
    }

    @Test
    public void requestScheme_valid_expectHttps() {
        AwsProxyRequest request = getRequestBuilder("/echo/scheme", "GET")
                .json()
                .build();

        AwsProxyResponse output = handler.proxy(request, lambdaContext);
        assertEquals(200, output.getStatusCode());
        assertEquals("application/json", output.getMultiValueHeaders().getFirst("Content-Type"));

        validateSingleValueModel(output, "https");
    }

    @Test
    public void authorizer_securityContext_customPrincipalSuccess() {
        AwsProxyRequest request = getRequestBuilder("/echo/authorizer-principal", "GET")
                .json()
                .authorizerPrincipal(AUTHORIZER_PRINCIPAL_ID)
                .build();

        AwsProxyResponse output = handler.proxy(request, lambdaContext);
        if (!isAlb) {
            assertEquals(200, output.getStatusCode());
            assertEquals("application/json", output.getMultiValueHeaders().getFirst("Content-Type"));
            validateSingleValueModel(output, AUTHORIZER_PRINCIPAL_ID);
        }


    }

    @Test
    public void authorizer_securityContext_customAuthorizerContextSuccess() {
        AwsProxyRequest request = getRequestBuilder("/echo/authorizer-context", "GET")
                .json()
                .authorizerPrincipal(AUTHORIZER_PRINCIPAL_ID)
                .authorizerContextValue(CUSTOM_HEADER_KEY, CUSTOM_HEADER_VALUE)
                .queryString("key", CUSTOM_HEADER_KEY)
                .build();

        AwsProxyResponse output = handler.proxy(request, lambdaContext);
        assertEquals(200, output.getStatusCode());
        assertEquals("application/json", output.getMultiValueHeaders().getFirst("Content-Type"));

        validateSingleValueModel(output, CUSTOM_HEADER_VALUE);
    }

    @Test
    public void errors_unknownRoute_expect404() throws ContainerInitializationException {
        MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
                ApplicationContext.build(CollectionUtils.mapOf(
                        "spec.name", "MicronautAwsProxyTest",
                        "micronaut.security.enabled", false,
                        "micronaut.views.handlebars.enabled", true,
                        "micronaut.router.static-resources.lorem.paths", "classpath:static-lorem/",
                        "micronaut.router.static-resources.lorem.mapping", "/static-lorem/**"
                ))
        );
        AwsProxyRequest request = getRequestBuilder("/echo/test33", "GET").build();

        AwsProxyResponse output = handler.proxy(request, lambdaContext);
        assertEquals(404, output.getStatusCode());
    }

    @Test
    public void error_statusCode_methodNotAllowed() throws ContainerInitializationException {
        MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
                ApplicationContext.build(CollectionUtils.mapOf(
                        "spec.name", "MicronautAwsProxyTest",
                        "micronaut.security.enabled", false,
                        "micronaut.views.handlebars.enabled", true,
                        "micronaut.router.static-resources.lorem.paths", "classpath:static-lorem/",
                        "micronaut.router.static-resources.lorem.mapping", "/static-lorem/**"
                ))
        );
        AwsProxyRequest request = getRequestBuilder("/echo/status-code", "POST")
                .json()
                .queryString("status", "201")
                .build();

        AwsProxyResponse output = handler.proxy(request, lambdaContext);
        assertEquals(405, output.getStatusCode());
    }

    @Test
    public void error_contentType_invalidContentType() {
        AwsProxyRequest request = getRequestBuilder("/echo/json-body", "POST")
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM)
                .body("asdasdasd")
                .build();

        AwsProxyResponse output = handler.proxy(request, lambdaContext);
        assertEquals(415, output.getStatusCode());
    }

    @Test
    public void responseBody_responseWriter_validBody() throws JsonProcessingException {
        SingleValueModel singleValueModel = new SingleValueModel();
        singleValueModel.setValue(CUSTOM_HEADER_VALUE);
        AwsProxyRequest request = getRequestBuilder("/echo/json-body", "POST")
                .json()
                .body(objectMapper.writeValueAsString(singleValueModel))
                .build();

        AwsProxyResponse output = handler.proxy(request, lambdaContext);
        assertEquals(200, output.getStatusCode());
        assertNotNull(output.getBody());

        validateSingleValueModel(output, CUSTOM_HEADER_VALUE);
    }

    @Test
    public void statusCode_responseStatusCode_customStatusCode() {
        AwsProxyRequest request = getRequestBuilder("/echo/status-code", "GET")
                .json()
                .queryString("status", "201")
                .build();

        AwsProxyResponse output = handler.proxy(request, lambdaContext);
        assertEquals(201, output.getStatusCode());
    }

    @Test
    public void base64_binaryResponse_base64Encoding() {
        AwsProxyRequest request = getRequestBuilder("/echo/binary", "GET").build();

        AwsProxyResponse response = handler.proxy(request, lambdaContext);
        assertNotNull(response.getBody());
        assertTrue(Base64.isBase64(response.getBody()));
    }

    @Test
    public void stripBasePath_route_shouldRouteCorrectly() {
        handler.stripBasePath("/custompath");
        try {
            AwsProxyRequest request = getRequestBuilder("/custompath/echo/status-code", "GET")
                    .json()
                    .queryString("status", "201")
                    .build();
            AwsProxyResponse output = handler.proxy(request, lambdaContext);
            assertEquals(201, output.getStatusCode());
        } finally {
            handler.stripBasePath("");
        }
    }

    @Test
    public void automaticStripBasePath_route_shouldRouteCorrectly() {
        handler.stripBasePath("/custompath");
        try {
            AwsProxyRequest request = getRequestBuilder("/custompath/echo/status-code", "GET")
                    .json()
                    .queryString("status", "201")
                    .build();
            request.setResource("/{proxy+}");
            request.setPathParameters(Collections.singletonMap("proxy", "echo/status-code"));

            AwsProxyResponse output = handler.proxy(request, lambdaContext);
            assertEquals(201, output.getStatusCode());
        } finally {
            handler.stripBasePath("");
        }
    }

    @Test
    public void automaticStripBasePath_route_shouldRouteCorrectly2() {
        handler.stripBasePath("/custompath");
        try {
            AwsProxyRequest request = getRequestBuilder("/custompath/echo/status-code", "GET")
                    .json()
                    .queryString("status", "201")
                    .build();

            request.setResource("/{controller}/{action}");

            Map<String, String> pathParameters = new HashMap<>();
            pathParameters.put("controller", "echo");
            pathParameters.put("action", "status-code");
            request.setPathParameters(pathParameters);

            AwsProxyResponse output = handler.proxy(request, lambdaContext);
            assertEquals(201, output.getStatusCode());
        } finally {
            handler.stripBasePath("");
        }
    }

    @Test
    public void stripBasePath_route_shouldReturn404() throws ContainerInitializationException {
        MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(
                ApplicationContext.build(CollectionUtils.mapOf(
                        "spec.name", "MicronautAwsProxyTest",
                        "micronaut.security.enabled", false,
                        "micronaut.views.handlebars.enabled", true,
                        "micronaut.router.static-resources.lorem.paths", "classpath:static-lorem/",
                        "micronaut.router.static-resources.lorem.mapping", "/static-lorem/**"
                ))
        );
        handler.stripBasePath("/custom");
        try {
            AwsProxyRequest request = getRequestBuilder("/custompath/echo/status-code", "GET")
                    .json()
                    .queryString("status", "201")
                    .build();
            AwsProxyResponse output = handler.proxy(request, lambdaContext);
            assertEquals(404, output.getStatusCode());
        } finally {

            handler.stripBasePath("");
        }
    }

    @Test
    public void securityContext_injectPrincipal_expectPrincipalName() {
        AwsProxyRequest request = getRequestBuilder("/echo/security-context", "GET")
                .authorizerPrincipal(USER_PRINCIPAL).build();

        AwsProxyResponse resp = handler.proxy(request, lambdaContext);
        assertEquals(200, resp.getStatusCode());
        validateSingleValueModel(resp, USER_PRINCIPAL);
    }

    @Test
    public void emptyStream_putNullBody_expectPutToSucceed() {
        AwsProxyRequest request = getRequestBuilder("/echo/empty-stream/" + CUSTOM_HEADER_KEY + "/test/2", "PUT")
                .nullBody()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .build();
        AwsProxyResponse resp = handler.proxy(request, lambdaContext);
        assertEquals(200, resp.getStatusCode());
        validateSingleValueModel(resp, CUSTOM_HEADER_KEY);
    }

    @Test
    public void refererHeader_headerParam_expectCorrectInjection() {
        String refererValue = "test-referer";
        AwsProxyRequest request = getRequestBuilder("/echo/referer-header", "GET")
                .nullBody()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .header("Referer", refererValue)
                .build();

        AwsProxyResponse resp = handler.proxy(request, lambdaContext);
        assertEquals(200, resp.getStatusCode());
        validateSingleValueModel(resp, refererValue);
    }

    @Test
    public void context_expectCorrectInjection() {
        AwsProxyRequest request = getRequestBuilder("/echo/lambda-context", "GET")
                .nullBody()
                .header(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON)
                .build();

        AwsProxyResponse resp = handler.proxy(request, lambdaContext);
        assertEquals(200, resp.getStatusCode());
        validateSingleValueModel(resp, "it works: null");
    }

    @Test
    public void renderEngineHtml() {
        AwsProxyRequest request = getRequestBuilder("/echo/render-html", "GET")
            .header("Content-Type", "text/html")
            .build();

        AwsProxyResponse resp = handler.proxy(request, lambdaContext);

        assertEquals(200, resp.getStatusCode());
        assertEquals("<html>Hello Luke Skywalker</html>", resp.getBody());
    }

    @Test
    public void static_resource() {
        AwsProxyRequest request = getRequestBuilder("/static-lorem/lorem.txt", "GET")
                .build();

        AwsProxyResponse resp = handler.proxy(request, lambdaContext);

        final String expectedBody = "Lorem ipsum";

        List<String> contentLength = resp.getMultiValueHeaders().get("Content-Length");

        assertEquals(200, resp.getStatusCode());
        assertEquals(expectedBody, resp.getBody());
        assertNotNull(contentLength);
        assertEquals(contentLength.get(0), String.valueOf(expectedBody.length()));
    }

    private void validateMapResponseModel(AwsProxyResponse output) {
        validateMapResponseModel(output, CUSTOM_HEADER_KEY, CUSTOM_HEADER_VALUE);
    }

    private void validateMapResponseModel(AwsProxyResponse output, String key, String value) {
        try {
            MapResponseModel response = objectMapper.readValue(output.getBody(), MapResponseModel.class);
            assertNotNull(response.getValues().get(key));
            assertEquals(value, response.getValues().get(key));
        } catch (IOException e) {
            fail("Exception while parsing response body: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void validateSingleValueModel(AwsProxyResponse output, String value) {
        try {
            SingleValueModel response = objectMapper.readValue(output.getBody(), SingleValueModel.class);
            assertNotNull(response.getValue());
            assertEquals(value, response.getValue());
        } catch (IOException e) {
            fail("Exception while parsing response body: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
