package io.micronaut.function.aws.proxy.payload2;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.convert.DefaultMutableConversionService;
import io.micronaut.core.util.StringUtils;
import io.micronaut.data.model.Pageable;
import io.micronaut.function.aws.proxy.MockLambdaContext;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MediaType;
import io.micronaut.http.MutableHttpRequest;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.netty.NettyMutableHttpResponse;
import io.micronaut.http.simple.cookies.SimpleCookie;
import io.micronaut.json.JsonMapper;
import io.micronaut.runtime.server.EmbeddedServer;
import jakarta.validation.Valid;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Test that a Lambda behind an AWS API Gateway V2 (or a Lambda function URL) returns the expected responses.
 * <p>
 * Most tests assert the same invariant: a controller must produce the same response whether the request reaches
 * Micronaut directly (embedded server) or is first translated by API Gateway V2 into a payload format version 2.0
 * event ({@link APIGatewayV2HTTPEvent}) and handled by {@link APIGatewayV2HTTPEventFunction}. This matters because
 * API Gateway V2 combines duplicate query strings with commas in "queryStringParameters", so the Lambda handler must
 * rely on "rawQueryString" to avoid misinterpreting values that legitimately contain commas.
 *
 * @see <a href="https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html#http-api-develop-integrations-lambda.proxy-format">
 *     Payload format version 2.0
 *     </a>
 * @see <a href="https://micronaut-projects.github.io/micronaut-aws/latest/guide/#lambdaTest">
 *     AWS Lambda Test
 *     </a>
 */
class ApiGatewayV2Test {

    private static final String SPEC_NAME = "ApiGatewayV2Test";

    /**
     * A payload format version 2.0 event for a GET request, as AWS API Gateway V2 sends it.
     * Parameters: 1) path, 2) rawQueryString, 3) queryStringParameters as a JSON object (or null).
     */
    private static final String GET_PAYLOAD_V2_JSON = """
        {
          "version": "2.0",
          "routeKey": "$default",
          "rawPath": "%1$s",
          "rawQueryString": "%2$s",
          "cookies": [
            "cookie1",
            "cookie2"
          ],
          "headers": {
            "accept": "*/*",
            "user-agent": "curl/8.7.1"
          },
          "queryStringParameters": %3$s,
          "requestContext": {
            "accountId": "123456789012",
            "apiId": "api-id",
            "domainName": "id.execute-api.us-east-1.amazonaws.com",
            "domainPrefix": "id",
            "http": {
              "method": "GET",
              "path": "%1$s",
              "protocol": "HTTP/1.1",
              "sourceIp": "192.0.2.1",
              "userAgent": "curl/8.7.1"
            },
            "requestId": "id",
            "routeKey": "$default",
            "stage": "$default",
            "time": "12/Mar/2020:19:03:58 +0000",
            "timeEpoch": 1583348638390
          },
          "pathParameters": {
            "parameter1": "value1"
          },
          "isBase64Encoded": false,
          "stageVariables": {
            "stageVariable1": "value1",
            "stageVariable2": "value2"
          }
        }""";

    /**
     * A payload format version 2.0 event for a form submission, as AWS API Gateway V2 sends it.
     * Parameters: 1) path, 2) the "application/x-www-form-urlencoded" body.
     */
    private static final String FORM_POST_PAYLOAD_V2_JSON = """
        {
          "version": "2.0",
          "routeKey": "$default",
          "rawPath": "%1$s",
          "rawQueryString": "",
          "cookies": [
            "cookie1",
            "cookie2"
          ],
          "headers": {
            "accept": "*/*",
            "content-type": "application/x-www-form-urlencoded",
            "user-agent": "curl/8.7.1"
          },
          "requestContext": {
            "accountId": "123456789012",
            "apiId": "api-id",
            "domainName": "id.execute-api.us-east-1.amazonaws.com",
            "domainPrefix": "id",
            "http": {
              "method": "POST",
              "path": "%1$s",
              "protocol": "HTTP/1.1",
              "sourceIp": "192.0.2.1",
              "userAgent": "curl/8.7.1"
            },
            "requestId": "id",
            "routeKey": "$default",
            "stage": "$default",
            "time": "12/Mar/2020:19:03:58 +0000",
            "timeEpoch": 1583348638390
          },
          "body": "%2$s",
          "pathParameters": {
            "parameter1": "value1"
          },
          "isBase64Encoded": false,
          "stageVariables": {
            "stageVariable1": "value1",
            "stageVariable2": "value2"
          }
        }""";

    @Requires(property = "spec.name", value = SPEC_NAME)
    @Controller("/test/lambdaApiGateway")
    static class TestLambdaApiGatewayController {

        @Get("/sample")
        HttpResponse<?> sampleGet() {
            return HttpResponse.ok("gotSampleLambdaValue-GET");
        }

        @Get("/multiCookie")
        MutableHttpResponse<?> multiCookie() {
            NettyMutableHttpResponse<String> rsp = new NettyMutableHttpResponse<>(new DefaultMutableConversionService());
            rsp.cookie(new SimpleCookie("cookie-num-1", "cookie-val-1"));
            rsp.cookie(new SimpleCookie("cookie-num-2", "cookie-val-2"));

            final String superCoolMultiValueHeaderName = "Super-Cool-Multi-Value-Header";
            rsp.header(superCoolMultiValueHeaderName, "multi-val-1");
            rsp.header(superCoolMultiValueHeaderName, "multi-val-2");

            rsp.body("cookie-response-body");
            return rsp;
        }

        @Get("/multiParameter")
        Map<String, Object> multiParameter(List<String> parameter1) {
            return Map.of("parameter1", parameter1);
        }

        @Get("/multiParameterNumeric")
        Map<String, Object> multiParameterNumeric(List<Integer> parameter1) {
            return Map.of("parameter1", parameter1);
        }

        @Post(value = "/bodyParameters", consumes = MediaType.APPLICATION_FORM_URLENCODED)
        Map<String, Object> bodyParameters(List<String> parameter1) {
            return Map.of("parameter1", parameter1);
        }

        @Get("/pageableTest")
        Map<String, Object> pageable(@Valid Pageable pageable) {
            // LinkedHashMap so the response body serializes with a deterministic key order
            Map<String, Object> result = new LinkedHashMap<>();
            result.put("page", pageable.getNumber());
            result.put("size", pageable.getSize());
            result.put("sort", pageable.getSort().getOrderBy().stream()
                .map(order -> order.getProperty() + " " + order.getDirection())
                .toList());
            return result;
        }
    }

    @Test
    void simpleGetRequest() throws IOException {
        String eventJson = getEventJson("/test/lambdaApiGateway/sample", "", "null");
        executeTest(eventJson, response ->
            assertEquals("gotSampleLambdaValue-GET", response.getBody()));
    }

    /**
     * Ensure that a response providing multiple cookies and headers still transmits more than one cookie in the
     * payload v2 response. In payload v2 format, "headers" is used, not "multiValueHeaders", and cookies belong in
     * "cookies", not in a "Set-Cookie" header.
     */
    @Test
    void multipleCookiesAndMultiValueHeadersInResponse() throws IOException {
        String eventJson = getEventJson("/test/lambdaApiGateway/multiCookie", "", "null");
        executeTest(eventJson, response -> {
            Map<String, String> headers = response.getHeaders();
            assertNotNull(headers);

            // In v2, multi-value headers are combined with commas ("val1,val2"). In v1, "multiValueHeaders" was a
            // Map<String, List<String>>.
            assertEquals("multi-val-1,multi-val-2", headers.get("Super-Cool-Multi-Value-Header"));

            // In v2, cookies should be in "cookies", not as a "Set-Cookie" header
            assertNull(headers.get(HttpHeaders.SET_COOKIE));

            List<String> cookies = response.getCookies();
            assertNotNull(cookies);
            assertEquals(2, cookies.size());
            assertTrue(cookies.get(0).contains("cookie-num-1=cookie-val-1"));
            assertTrue(cookies.get(1).contains("cookie-num-2=cookie-val-2"));

            assertEquals("cookie-response-body", response.getBody());
        });
    }

    /**
     * Two values for "parameter1", where the first value has a URL-encoded comma in it. That comma should NOT cause
     * the two values to be interpreted as three values, even though API Gateway V2 provides
     * "queryStringParameters": {"parameter1": "value1,value2,value3"} for this request.
     */
    @Test
    void urlEncodedCommaInRepeatedQueryParameterIsNotADelimiter() throws IOException {
        // curl 'https://0123456789.execute-api.us-east-1.amazonaws.com/test/lambdaApiGateway/multiParameter?parameter1=value1%2Cvalue2&parameter1=value3'
        assertSameResponseViaApiGatewayV2AndDirectRequest(
            getEventJson(
                "/test/lambdaApiGateway/multiParameter",
                "parameter1=value1%2Cvalue2&parameter1=value3",
                """
                {"parameter1": "value1,value2,value3"}"""),
            """
            {"parameter1":["value1,value2","value3"]}""");
    }

    /**
     * Same as {@link #urlEncodedCommaInRepeatedQueryParameterIsNotADelimiter()}, but the comma in the first value is
     * not URL-encoded.
     */
    @Test
    void unencodedCommaInRepeatedQueryParameterIsNotADelimiter() throws IOException {
        assertSameResponseViaApiGatewayV2AndDirectRequest(
            getEventJson(
                "/test/lambdaApiGateway/multiParameter",
                "parameter1=value1,value2&parameter1=value3",
                """
                {"parameter1": "value1,value2,value3"}"""),
            """
            {"parameter1":["value1,value2","value3"]}""");
    }

    /**
     * When the parameter key is only provided once, Micronaut assumes by default that the comma is a delimiter when
     * binding to a {@code List<String>}, so two values are expected.
     */
    @Test
    void urlEncodedCommaInSingleQueryParameterIsADelimiter() throws IOException {
        assertSameResponseViaApiGatewayV2AndDirectRequest(
            getEventJson(
                "/test/lambdaApiGateway/multiParameter",
                "parameter1=value1%2Cvalue2",
                """
                {"parameter1": "value1,value2"}"""),
            """
            {"parameter1":["value1","value2"]}""");
    }

    /**
     * Same as {@link #urlEncodedCommaInSingleQueryParameterIsADelimiter()}, but the comma is not URL-encoded.
     */
    @Test
    void unencodedCommaInSingleQueryParameterIsADelimiter() throws IOException {
        assertSameResponseViaApiGatewayV2AndDirectRequest(
            getEventJson(
                "/test/lambdaApiGateway/multiParameter",
                "parameter1=value1,value2",
                """
                {"parameter1": "value1,value2"}"""),
            """
            {"parameter1":["value1","value2"]}""");
    }

    @Test
    void urlEncodedSpaceInQueryParameterValueIsDecoded() throws IOException {
        assertSameResponseViaApiGatewayV2AndDirectRequest(
            getEventJson(
                "/test/lambdaApiGateway/multiParameter",
                "parameter1=value1%20value2",
                """
                {"parameter1": "value1 value2"}"""),
            """
            {"parameter1":["value1 value2"]}""");
    }

    /**
     * For "application/x-www-form-urlencoded", form bodies are "parameter bags", not multi-maps, so only a single
     * value is retained for a repeated key. The URL-encoded comma in that value is part of the value, not a delimiter.
     */
    @Test
    void formBodyUrlEncodedCommaWithRepeatedKeysIsNotADelimiter() throws IOException {
        assertSameResponseViaApiGatewayV2AndDirectRequest(
            formPostEventJson(
                "/test/lambdaApiGateway/bodyParameters",
                "parameter1=value1%2Cvalue2&parameter1=value3"),
            """
            {"parameter1":["value1,value2"]}""");
    }

    /**
     * Same as {@link #formBodyUrlEncodedCommaWithRepeatedKeysIsNotADelimiter()}, but the comma is not URL-encoded.
     */
    @Test
    void formBodyUnencodedCommaWithRepeatedKeysIsNotADelimiter() throws IOException {
        assertSameResponseViaApiGatewayV2AndDirectRequest(
            formPostEventJson(
                "/test/lambdaApiGateway/bodyParameters",
                "parameter1=value1,value2&parameter1=value3"),
            """
            {"parameter1":["value1,value2"]}""");
    }

    /**
     * Unlike query parameters, a comma in a form body value is never treated as a delimiter, so a single value with a
     * comma in it is expected.
     */
    @Test
    void formBodyUrlEncodedCommaInSingleParameterIsNotADelimiter() throws IOException {
        assertSameResponseViaApiGatewayV2AndDirectRequest(
            formPostEventJson(
                "/test/lambdaApiGateway/bodyParameters",
                "parameter1=value1%2Cvalue2"),
            """
            {"parameter1":["value1,value2"]}""");
    }

    /**
     * Same as {@link #formBodyUrlEncodedCommaInSingleParameterIsNotADelimiter()}, but the comma is not URL-encoded.
     */
    @Test
    void formBodyUnencodedCommaInSingleParameterIsNotADelimiter() throws IOException {
        assertSameResponseViaApiGatewayV2AndDirectRequest(
            formPostEventJson(
                "/test/lambdaApiGateway/bodyParameters",
                "parameter1=value1,value2"),
            """
            {"parameter1":["value1,value2"]}""");
    }

    @Test
    void formBodyUrlEncodedSpaceIsDecoded() throws IOException {
        assertSameResponseViaApiGatewayV2AndDirectRequest(
            formPostEventJson(
                "/test/lambdaApiGateway/bodyParameters",
                "parameter1=value1%20value2"),
            """
            {"parameter1":["value1 value2"]}""");
    }

    @Test
    void commaSeparatedNumericQueryParameterBindsToListOfIntegers() throws IOException {
        assertSameResponseViaApiGatewayV2AndDirectRequest(
            getEventJson(
                "/test/lambdaApiGateway/multiParameterNumeric",
                "parameter1=3,4,5",
                """
                {"parameter1": "3,4,5"}"""),
            """
            {"parameter1":[3,4,5]}""");
    }

    @Test
    void repeatedNumericQueryParameterBindsToListOfIntegers() throws IOException {
        assertSameResponseViaApiGatewayV2AndDirectRequest(
            getEventJson(
                "/test/lambdaApiGateway/multiParameterNumeric",
                "parameter1=3&parameter1=4&parameter1=5",
                """
                {"parameter1": "3,4,5"}"""),
            """
            {"parameter1":[3,4,5]}""");
    }

    /**
     * Test objects with specialized binders, specifically {@link Pageable} (PageableRequestArgumentBinder). There is
     * a comma, not a space, between "created" and "desc", which is how {@link Pageable} expects it. The comma should
     * not split into two separate values, even though it normally does for a single-key parameter bound to a
     * {@code List<String>}, because {@link Pageable} is not bound via core collection splitting.
     */
    @Test
    void pageableSortParameterWithUrlEncodedCommaIsNotSplit() throws IOException {
        assertSameResponseViaApiGatewayV2AndDirectRequest(
            getEventJson(
                "/test/lambdaApiGateway/pageableTest",
                "page=3&size=5&sort=created%2Cdesc",
                """
                {"page": "3", "size": "5", "sort": "created,desc"}"""),
            """
            {"page":3,"size":5,"sort":["created DESC"]}""");
    }

    @Test
    void pageableWithMultipleSortParameters() throws IOException {
        assertSameResponseViaApiGatewayV2AndDirectRequest(
            getEventJson(
                "/test/lambdaApiGateway/pageableTest",
                "page=3&size=5&sort=created%2Cdesc&sort=id%2Casc",
                """
                {"page": "3", "size": "5", "sort": "created,desc,id,asc"}"""),
            """
            {"page":3,"size":5,"sort":["created DESC","id ASC"]}""");
    }

    /**
     * @param path the request path, with a leading slash
     * @param rawQueryString the query string exactly as the client sent it
     * @param queryStringParametersJson what AWS API Gateway V2 translates the "rawQueryString" into, as a JSON object
     *            (or "null"). Duplicate query strings are combined with commas, so this translation is lossy: it is
     *            impossible to tell a separator comma from a comma within a value. The handler is expected to use
     *            "rawQueryString" instead.
     */
    private static String getEventJson(String path, String rawQueryString, String queryStringParametersJson) {
        return GET_PAYLOAD_V2_JSON.formatted(path, rawQueryString, queryStringParametersJson);
    }

    private static String formPostEventJson(String path, String formUrlEncodedBody) {
        return FORM_POST_PAYLOAD_V2_JSON.formatted(path, formUrlEncodedBody);
    }

    private static void executeTest(String eventJson, Consumer<APIGatewayV2HTTPResponse> responseConsumer) throws IOException {
        try (
            ApplicationContext ctx = ApplicationContext.builder().properties(testConfig()).build();
            APIGatewayV2HTTPEventFunction handler = new APIGatewayV2HTTPEventFunction(ctx)
        ) {
            APIGatewayV2HTTPResponse response = handler.handleRequest(readEvent(ctx, eventJson), new MockLambdaContext());
            assertNotNull(response);
            assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
            responseConsumer.accept(response);
        }
    }

    /**
     * Asserts that the controller responds with {@code expectedBody} both when the equivalent HTTP request is made
     * directly against an embedded server and when the request is delivered as an API Gateway V2 payload through the
     * Lambda handler. The two must match: putting API Gateway V2 (or a Lambda function URL) in front of Micronaut
     * must not change what a controller receives.
     */
    private static void assertSameResponseViaApiGatewayV2AndDirectRequest(String eventJson, String expectedBody) throws IOException {
        try (
            ApplicationContext ctx = ApplicationContext.builder().properties(testConfig()).build();
            APIGatewayV2HTTPEventFunction handler = new APIGatewayV2HTTPEventFunction(ctx)
        ) {
            APIGatewayV2HTTPEvent event = readEvent(ctx, eventJson);

            String directBody = executeDirectRequest(ctx, event);
            assertEquals(expectedBody, directBody,
                () -> "Unexpected response for a direct request, bypassing API Gateway. Event: " + eventJson);

            APIGatewayV2HTTPResponse response = handler.handleRequest(event, new MockLambdaContext());
            assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
            assertEquals(expectedBody, response.getBody(),
                () -> "Unexpected response for a request through API Gateway V2. Event: " + eventJson);
        }
    }

    private static Map<String, Object> testConfig() {
        return Map.of(
            "micronaut.security.enabled", StringUtils.FALSE,
            "micronaut.security.csrf.filter.enabled", StringUtils.FALSE,
            "micronaut.server.port", "-1",
            "spec.name", SPEC_NAME
        );
    }

    private static APIGatewayV2HTTPEvent readEvent(ApplicationContext ctx, String eventJson) throws IOException {
        return ctx.getBean(JsonMapper.class).readValue(eventJson, APIGatewayV2HTTPEvent.class);
    }

    /**
     * Executes the request described by the event directly against an embedded server, bypassing the API Gateway V2
     * payload translation, and returns the response body.
     */
    private static String executeDirectRequest(ApplicationContext ctx, APIGatewayV2HTTPEvent event) {
        EmbeddedServer server = ctx.getBean(EmbeddedServer.class);
        if (!server.isRunning()) {
            server.start();
        }
        try (HttpClient client = HttpClient.create(server.getURL())) {
            HttpResponse<String> response = client.toBlocking().exchange(toDirectRequest(event), String.class);
            assertEquals(HttpStatus.OK, response.getStatus());
            return response.body();
        }
    }

    private static MutableHttpRequest<?> toDirectRequest(APIGatewayV2HTTPEvent apiGatewayV2HttpEvent) {
        String rawPath = apiGatewayV2HttpEvent.getRawPath();
        String rawQueryString = apiGatewayV2HttpEvent.getRawQueryString();
        String httpMethod = apiGatewayV2HttpEvent.getRequestContext().getHttp().getMethod();

        MutableHttpRequest<?> request;
        if (HttpMethod.GET.name().equals(httpMethod)) {
            request = HttpRequest.GET(StringUtils.isNotEmpty(rawQueryString) ? rawPath + "?" + rawQueryString : rawPath);
        } else if (HttpMethod.POST.name().equals(httpMethod)) {
            request = HttpRequest.POST(rawPath, apiGatewayV2HttpEvent.getBody() == null ? "" : apiGatewayV2HttpEvent.getBody());
        } else {
            throw new IllegalArgumentException("Unsupported HTTP method: " + httpMethod);
        }

        String contentType = apiGatewayV2HttpEvent.getHeaders() == null ?
            null :
            apiGatewayV2HttpEvent.getHeaders().get(HttpHeaders.CONTENT_TYPE.toLowerCase());
        if (contentType != null) {
            request.contentType(contentType);
        }
        return request;
    }
}
