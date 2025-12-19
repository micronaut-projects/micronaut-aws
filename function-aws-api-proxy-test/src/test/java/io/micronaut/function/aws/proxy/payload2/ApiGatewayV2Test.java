package io.micronaut.function.aws.proxy.payload2;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.env.Environment;
import io.micronaut.core.convert.DefaultMutableConversionService;
import io.micronaut.function.aws.MicronautLambdaContext;
import io.micronaut.function.aws.proxy.MockLambdaContext;
import io.micronaut.http.*;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.netty.NettyMutableHttpResponse;
import io.micronaut.http.simple.cookies.SimpleCookie;
import io.micronaut.json.JsonMapper;
import io.micronaut.runtime.server.EmbeddedServer;
import io.micronaut.scheduling.TaskExecutors;
import io.micronaut.scheduling.annotation.ExecuteOn;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;
import io.micronaut.servlet.http.ServletExchange;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;

/**
 * Test that a Lambda API Gateway returns the expected responses.
 * We're not running this as a @MicronautTest because APIGatewayV2HTTPEventFunction starts up its own ApplicationContext
 * @see <a href="https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html#http-api-develop-integrations-lambda.proxy-format">
 *     Payload format version 2.0
 *     </a>
 * @see <a href="https://micronaut-projects.github.io/micronaut-aws/latest/guide/#lambdaTest">
 *     AWS Lambda Test
 *     </a>
 */
public class ApiGatewayV2Test {

	/**
	 * The APIGatewayV2HTTPEventFunction, as indicated as the MicronautFunction handler in LambdaConstruct.java
	 */
	private static APIGatewayV2HTTPEventFunction handler;

	@BeforeAll
	public static void setupSpec() {
		handler = new TestAPIGatewayV2HTTPEventFunction();
	}

	@AfterAll
	public static void cleanupSpec() {
		handler.getApplicationContext().close();
	}

	// Custom handler that also includes the "test" environment
	private static class TestAPIGatewayV2HTTPEventFunction extends APIGatewayV2HTTPEventFunction {
		@Override
		protected ApplicationContextBuilder newApplicationContextBuilder() {
			ApplicationContextBuilder builder = super.newApplicationContextBuilder();
			// We're overriding the builder so we can also include the "test" environment
			builder.environments(
					Environment.FUNCTION,
					MicronautLambdaContext.ENVIRONMENT_LAMBDA,
					Environment.TEST
			);
			builder.properties(Map.of(
					//"micronaut.security.csrf.enabled", "false",
					//"micronaut.server.cors.enabled", "false",
					//"micronaut.security.csrf.filter.regex-pattern", "^(?!.*(/test/lambdaApiGateway/bodyParameters)).*$",
					"micronaut.security.csrf.filter.enabled", "false"
			));
			return builder;
		}
	}

	@ExecuteOn(TaskExecutors.IO)
	@Controller("/test/lambdaApiGateway")
	public static class TestLambdaApiGatewayController {
		@Secured(SecurityRule.IS_ANONYMOUS)
		@Get("/sample")
		public HttpResponse<?> sampleGet() {
			return HttpResponse.ok("gotSampleLambdaValue-GET");
		}

		@Secured(SecurityRule.IS_ANONYMOUS)
		@Get("/multiCookie")
		public MutableHttpResponse<?> sampleMultiCookieGet() {
			NettyMutableHttpResponse<String> rsp = new NettyMutableHttpResponse<>(new DefaultMutableConversionService());
			rsp.cookie(new SimpleCookie("cookie-num-1", "cookie-val-1"));
			rsp.cookie(new SimpleCookie("cookie-num-2", "cookie-val-2"));

			final String superCoolMultiValueHeaderName = "Super-Cool-Multi-Value-Header";
			rsp.header(superCoolMultiValueHeaderName, "multi-val-1");
			rsp.header(superCoolMultiValueHeaderName, "multi-val-2");

			rsp.body("cookie-response-body");
			return rsp;
		}

		@Secured(SecurityRule.IS_ANONYMOUS)
		@Get("/multiParameter")
		public Map<String, Object> sampleMultiValueParameter(List<String> parameter1) {
			return Map.of("parameter1", parameter1);
		}

		@Secured(SecurityRule.IS_ANONYMOUS)
		@Post(value = "/bodyParameters", consumes = MediaType.APPLICATION_FORM_URLENCODED)
		public Map<String, Object> sendPasswordResetLinkEmail(List<String> parameter1) {
			return Map.of("parameter1", parameter1);
		}
	}

	@Test
	public void testSimpleGetResponse() {
		APIGatewayV2HTTPEvent request = new APIGatewayV2HTTPEvent();
		request.setRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder()
				.withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder()
						.withPath("/test/lambdaApiGateway/sample")
						.withMethod(HttpMethod.GET.toString())
						.build()
				).build());
		APIGatewayV2HTTPResponse response = handler.handleRequest(request, new MockLambdaContext());

		assertEquals(HttpStatus.OK.getCode(), response.getStatusCode());
		assertEquals("gotSampleLambdaValue-GET", response.getBody());
	}

	/**
	 * Ensure that a response providing multiple cookies and headers still transmits more than one cookie in the Payload
	 * v2 response
	 */
	@Test
	public void testMultipleCookieAndHeaderResponse() {
		APIGatewayV2HTTPEvent request = new APIGatewayV2HTTPEvent();
		request.setRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder()
				.withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder()
						.withPath("/test/lambdaApiGateway/multiCookie")
						.withMethod(HttpMethod.GET.toString())
						.build()
				).build());
		APIGatewayV2HTTPResponse rsp2 = handler.handleRequest(request, new MockLambdaContext());

		assertEquals(HttpStatus.OK.getCode(), rsp2.getStatusCode());

		// In Payload v2 format, "headers" is used, not "multiValueHeaders". And cookies should be in "cookies", not
		// as a "Set-Cookie" header.
		// See https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html#http-api-develop-integrations-lambda.proxy-format

		// Use "headers" not "multiValueHeaders" in v2 format
		Map<String, String> headers = rsp2.getHeaders();
		Assertions.assertNotNull(headers);

		String multiValueHeaderInV2Format = headers.get("Super-Cool-Multi-Value-Header");
		// In v2, the format is "val1,val2". In v1, it was a Map<String, List<String>>.
		Assertions.assertEquals("multi-val-1,multi-val-2", multiValueHeaderInV2Format);

		// In v2, cookies should be in "cookies" not as a "Set-Cookie" header
		Assertions.assertNull(headers.get(HttpHeaders.SET_COOKIE));

		List<String> cookies = rsp2.getCookies();
		Assertions.assertNotNull(cookies);
		Assertions.assertEquals(2, cookies.size());
		String cookie1 = cookies.get(0);
		String cookie2 = cookies.get(1);

		Assertions.assertTrue(cookie1.contains("cookie-num-1=cookie-val-1"));
		Assertions.assertTrue(cookie2.contains("cookie-num-2=cookie-val-2"));

		assertEquals("cookie-response-body",  rsp2.getBody());
	}

	/**
	 * @param rawParameters
	 * @param queryStringParameters what AWS API Gateway V2 translates the "rawQueryString" into
	 * @param expectedValuesFromQueryString what we expect the controller to receive and respond with when the raw
	 *            parameters are provided in a query string. QueryStringDecoder is used, backed by a
	 *            {Map&lt;String, List&lt;String&gt;&gt;}. Repeated keys are preserved. Comma-splitting only occurs if
	 *            the key is only provided once.
	 * @param expectedValuesFromBodyString what we expect the controller to receive and respond with when the raw
	 *            parameters are provided in the request body. For "application/x-www-form-urlencoded", form bodies are
	 *            "parameter bags", not multi-maps, so repeated keys are overwritten. Comma-splitting will occur on the
	 *            single-value.
	 */
	private record QueryStringValues(
		String rawParameters,
		Map<String, String> queryStringParameters,
		List<String> expectedValuesFromQueryString,
		List<String> expectedValuesFromBodyString
	) {}

	private static final List<QueryStringValues> QUERY_STRINGS_AND_EXPECTED_VALUES = List.of(
			new QueryStringValues(
					// Should be two values for "parameter1", where the first value has URL-encoded comma in it. That
					// comma should NOT cause these two values to be interpreted as three values.
					"parameter1=value1%2Cvalue2&parameter1=value3",
					// The following "queryStringParameters" value is not desired since we no longer have two values,
					// where the first has a comma in it, but it's what AWS API Gateway provides for a request with the
					// above raw query string.
					Map.of("parameter1", "value1,value2,value3"),
					List.of(
							"value1,value2",
							"value3"
					),
					List.of(
							"value1",
							"value2"
					)
			),
			new QueryStringValues(
					// Should be two values for "parameter1", where the first value has non-URL-encoded comma in it.
					// That comma should NOT cause these two values to be interpreted as three values.
					"parameter1=value1,value2&parameter1=value3",
					// The following "queryStringParameters" value is not desired since we no longer have two values,
					// where the first has a comma in it, but it's what AWS API Gateway provides for a request with the
					// above raw query string.
					Map.of("parameter1", "value1,value2,value3"),
					List.of(
							"value1,value2",
							"value3"
					),
					List.of(
							"value1",
							"value2"
					)
			),
			// In this case, since the parameter key is only used once, Micronaut assumes by default that the comma is a
			// delimiter and provides two values.
			new QueryStringValues(
					// Should be two values for "parameter1" since the "parameter1" key is only found once and commas
					// are delimiters by default in that case.
					"parameter1=value1%2Cvalue2",
					// In this case, the "queryStringParameters" value does match the desired result, at least for the
					// default Micronaut parameter value implementation.
					Map.of("parameter1", "value1,value2"),
					List.of(
							"value1",
							"value2"
					),
					List.of(
							"value1",
							"value2"
					)
			),
			// In this case, since the parameter key is only used once, Micronaut assumes by default that the comma is a
			// delimiter and provides two values.
			new QueryStringValues(
					// Should be two values for "parameter1" since the "parameter1" key is only found once and commas
					// are delimiters by default in that case.
					"parameter1=value1,value2",
					// In this case, the "queryStringParameters" value does match the desired result, at least for the
					// default Micronaut parameter value implementation.
					Map.of("parameter1", "value1,value2"),
					List.of(
							"value1",
							"value2"
					),
					List.of(
							"value1",
							"value2"
					)
			)
	);

	/**
	 * Test that a "rawQueryString" of "parameter1=value1&parameter1=value2&parameter2=value" follows API Gateway v2's
	 * expected format of "queryStringParameters": { "parameter1": "value1,value2", "parameter2": "value" }
	 * Also ensure that if a comma is included in the parameter values, whether URI encoded or not, it is treated as
	 * part of the string. This is important because API Gateway V2 provides comma-separated values for
	 * "queryStringParameters".
	 * @see <a href="https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html#http-api-develop-integrations-lambda.proxy-format">
	 *     Payload format version 2.0
	 *     </a>
	 */
	@Test
	public void testQueryStringParameters() {
		// Create sample events with info that AWS API Gateway provides for e.g.
		// curl 'https://0123456789.execute-api.us-east-1.amazonaws.com/test/lambdaApiGateway/multiParameter?parameter1=value1%2Cvalue2&parameter1=value3'

		for (QueryStringValues queryStringValues : QUERY_STRINGS_AND_EXPECTED_VALUES) {
			APIGatewayV2HTTPEvent apiGatewayV2HttpEvent = sampleEventBuilderWithoutQueryStringValues(
					HttpMethod.GET,
					"test/lambdaApiGateway/multiParameter"
			)
					.withRawQueryString(queryStringValues.rawParameters())
					.withQueryStringParameters(queryStringValues.queryStringParameters())
					.build();

			assertParamValuesMatchAndResponseContainsParams(
					apiGatewayV2HttpEvent,
					"parameter1",
					queryStringValues
			);
		}
	}

	@Test
	public void testFormSubmissionBodyParameters() {
		for (QueryStringValues queryStringValues : QUERY_STRINGS_AND_EXPECTED_VALUES) {
			APIGatewayV2HTTPEvent apiGatewayV2HttpEvent = sampleEventBuilderWithoutQueryStringValues(
					HttpMethod.POST,
					"test/lambdaApiGateway/bodyParameters"
			)
					.withHeaders(Map.of(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_FORM_URLENCODED))
					.withBody(queryStringValues.rawParameters())
					.withRawQueryString(null)
					.withQueryStringParameters(null)
					.build();

			assertParamValuesMatchAndResponseContainsParams(
					apiGatewayV2HttpEvent,
					"parameter1",
					queryStringValues
			);
		}
	}

	private static APIGatewayV2HTTPEvent.APIGatewayV2HTTPEventBuilder sampleEventBuilderWithoutQueryStringValues(
			HttpMethod httpMethod, String rawPathWithoutLeadingSlash
	) {
		return APIGatewayV2HTTPEvent.builder()
				.withRawPath("/" + rawPathWithoutLeadingSlash)
				.withPathParameters(Map.of("proxy", rawPathWithoutLeadingSlash))
				.withStageVariables(null)
				.withRequestContext(APIGatewayV2HTTPEvent.RequestContext.builder()
						.withHttp(APIGatewayV2HTTPEvent.RequestContext.Http.builder()
								.withPath("/" + rawPathWithoutLeadingSlash)
								.withMethod(httpMethod.toString())
								.build()
						).build()
				);
	}

	private void assertParamValuesMatchAndResponseContainsParams(
			APIGatewayV2HTTPEvent apiGatewayV2HttpEvent, String paramName, QueryStringValues queryStringValues
	) {
		assertAsDirectRequest(apiGatewayV2HttpEvent, paramName, queryStringValues);
		assertParamValuesMatch(apiGatewayV2HttpEvent, paramName, queryStringValues);
		assertAsApiGatewayV2Request(apiGatewayV2HttpEvent, paramName, queryStringValues);
	}

	private void assertParamValuesMatch(
			APIGatewayV2HTTPEvent apiGatewayV2HttpEvent, String paramName, QueryStringValues queryStringValues
	) {
		ApplicationContext appCtx = handler.getApplicationContext();
		APIGatewayV2HTTPEventHandler apiGatewayV2HTTPEventHandler = appCtx.getBean(APIGatewayV2HTTPEventHandler.class);

		ServletExchange<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> req = apiGatewayV2HTTPEventHandler
				.createExchange(apiGatewayV2HttpEvent, null);

		HttpParameters params = req.getRequest().getParameters();
		Assertions.assertNotNull(params);

		List<String> values = params.getAll(paramName);

		Assertions.assertNotNull(values);
		String httpMethod = apiGatewayV2HttpEvent.getRequestContext().getHttp().getMethod();
		List<String> expectedValues = findExpectedValues(apiGatewayV2HttpEvent, queryStringValues);
		Assertions.assertEquals(expectedValues.size(), values.size(), "APIGatewayV2: Number of parameters does not match expected. Found " + values + ". Desired " + expectedValues + ". HttpMethod: " + httpMethod + ". RawParameters: " + queryStringValues.rawParameters());
		Assertions.assertEquals(expectedValues, values, "APIGatewayV2: Expected values do not match. HttpMethod: " + httpMethod + ". RawParameters: " + queryStringValues.rawParameters());
	}

	/**
	 * Test that a request that passes through API Gateway V2 first responds with the expected results
	 */
	private void assertAsApiGatewayV2Request(
			APIGatewayV2HTTPEvent apiGatewayV2HttpEvent, String paramName, QueryStringValues queryStringValues
	) {
		// Now submit the request and see what results we get
		APIGatewayV2HTTPResponse rsp = handler.handleRequest(apiGatewayV2HttpEvent, new MockLambdaContext());
		assertEquals(HttpStatus.OK.getCode(), rsp.getStatusCode());

		String body = rsp.getBody();
		assertResponseBody("apiGatewayV2Request", apiGatewayV2HttpEvent, paramName, queryStringValues, body);
	}

	private static HttpRequest<?> convertToHttpRequest(APIGatewayV2HTTPEvent apiGatewayV2HttpEvent) {
		String rawPath = apiGatewayV2HttpEvent.getRawPath();
		String rawQueryString = apiGatewayV2HttpEvent.getRawQueryString();
		String eventBody = apiGatewayV2HttpEvent.getBody();

		String httpMethod = apiGatewayV2HttpEvent.getRequestContext().getHttp().getMethod();
		String contentType = apiGatewayV2HttpEvent.getHeaders() == null ?
				null :
				apiGatewayV2HttpEvent.getHeaders().get(HttpHeaders.CONTENT_TYPE);

		MutableHttpRequest<?> request;
		if (HttpMethod.GET.toString().equals(httpMethod)) {
			request = HttpRequest.GET(rawPath + (rawQueryString != null ? "?" + rawQueryString : ""));
		} else if (HttpMethod.POST.toString().equals(httpMethod)) {
			request = HttpRequest.POST(rawPath, eventBody != null ? eventBody : "");
		} else {
			throw new IllegalArgumentException("Unsupported HTTP method: " + httpMethod);
		}

		if (contentType != null) {
			request.contentType(contentType);
		}
		return request;
	}

	private static List<String> findExpectedValues(
			APIGatewayV2HTTPEvent apiGatewayV2HttpEvent, QueryStringValues queryStringValues
	) {
		String httpMethod = apiGatewayV2HttpEvent.getRequestContext().getHttp().getMethod();

		List<String> expectedValues;
		if (HttpMethod.GET.toString().equals(httpMethod)) {
			expectedValues = queryStringValues.expectedValuesFromQueryString();
		} else if (HttpMethod.POST.toString().equals(httpMethod)) {
			expectedValues = queryStringValues.expectedValuesFromBodyString();
		} else {
			throw new IllegalArgumentException("Unsupported HTTP method: " + httpMethod);
		}

		return expectedValues;
	}

	/**
	 * Test that a standard request that does not use API Gateway V2 responds with the expected results
	 */
	private void assertAsDirectRequest(
			APIGatewayV2HTTPEvent apiGatewayV2HttpEvent, String paramName, QueryStringValues queryStringValues
	) {
		HttpRequest<?> request = convertToHttpRequest(apiGatewayV2HttpEvent);

		EmbeddedServer server = handler.getApplicationContext().getBean(EmbeddedServer.class);
		if (!server.isRunning()) {
			server.start();
		}

		// Create an HttpClient whose base URL behaves like @Client("/")
		HttpClient client = HttpClient.create(server.getURL());

		HttpResponse<String> rsp = client.toBlocking().exchange(request, String.class);
		Assertions.assertEquals(HttpStatus.OK, rsp.getStatus());

		String rspBody = rsp.body();
		assertResponseBody("directRequest", apiGatewayV2HttpEvent, paramName, queryStringValues, rspBody);
	}

	private void assertResponseBody(
			String requestType, APIGatewayV2HTTPEvent apiGatewayV2HttpEvent, String paramName,
			QueryStringValues queryStringValues, String body
	) {
		Assertions.assertNotNull(body);

		JsonMapper jsonMapper = JsonMapper.createDefault();
		final Map<String, Object> result;
		try {
			result = jsonMapper.readValue(body, Map.class);
		} catch (IOException e) {
			throw new RuntimeException("Unable ready body as JSON into Map [" + body + "]", e);
		}

		Assertions.assertNotNull(result);
		List<String> expectedValues = findExpectedValues(apiGatewayV2HttpEvent, queryStringValues);

		Object resultValuesObj = result.get(paramName);
		if (resultValuesObj instanceof List resultValues) {
			Assertions.assertNotNull(resultValues);
			Assertions.assertEquals(expectedValues.size(), resultValues.size(), "Response did not contain expected values. Request type: " + requestType + ". Found " + resultValues + ". Desired " + expectedValues + ". RawParameters: " + queryStringValues.rawParameters());

			Assertions.assertEquals(expectedValues, resultValues, "Unexpected response for RawParameters. Request type: " + requestType + ": " + queryStringValues.rawParameters());
		} else {
			Assertions.fail("Response did not contain expected values. Found " + resultValuesObj + ". Desired " + expectedValues + ".");
		}
	}
}
