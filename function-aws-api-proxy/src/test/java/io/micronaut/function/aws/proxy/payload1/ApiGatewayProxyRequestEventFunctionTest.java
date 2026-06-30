package io.micronaut.function.aws.proxy.payload1;

import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.aws.proxy.utils.MockContext;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.http.cookie.Cookie;
import io.micronaut.http.cookie.Cookies;
import io.micronaut.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class ApiGatewayProxyRequestEventFunctionTest {
    // https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html#http-api-develop-integrations-lambda.proxy-format
    private static final String PAYLOAD_V1_JSON = """
        {
            "version": "1.0",
            "resource": "%1$s",
            "path": "%1$s",
            "httpMethod": "POST",
            "headers": {
              "header1": "value1",
              "header2": "value2"
            },
            "multiValueHeaders": {
              "header1": [
                "value1"
              ],
              "header2": [
                "value1",
                "value2"
              ]
            },
            "queryStringParameters": {
              "parameter1": "value1",
              "parameter2": "value"
            },
            "multiValueQueryStringParameters": {
              "parameter1": [
                "value1",
                "value2"
              ],
              "parameter2": [
                "value"
              ]
            },
            "requestContext": {
              "accountId": "123456789012",
              "apiId": "id",
              "authorizer": {
                "claims": null,
                "scopes": null
              },
              "domainName": "id.execute-api.us-east-1.amazonaws.com",
              "domainPrefix": "id",
              "extendedRequestId": "request-id",
              "httpMethod": "POST",
              "identity": {
                "accessKey": null,
                "accountId": null,
                "caller": null,
                "cognitoAuthenticationProvider": null,
                "cognitoAuthenticationType": null,
                "cognitoIdentityId": null,
                "cognitoIdentityPoolId": null,
                "principalOrgId": null,
                "sourceIp": "192.0.2.1",
                "user": null,
                "userAgent": "user-agent",
                "userArn": null,
                "clientCert": {
                  "clientCertPem": "CERT_CONTENT",
                  "subjectDN": "www.example.com",
                  "issuerDN": "Example issuer",
                  "serialNumber": "a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1:a1",
                  "validity": {
                    "notBefore": "May 28 12:30:02 2019 GMT",
                    "notAfter": "Aug  5 09:36:04 2021 GMT"
                  }
                }
              },
              "path": "%1$s",
              "protocol": "HTTP/1.1",
              "requestId": "id=",
              "requestTime": "04/Mar/2020:19:15:17 +0000",
              "requestTimeEpoch": 1583349317135,
              "resourceId": null,
              "resourcePath": "/my/path",
              "stage": "$default"
            },
            "pathParameters": null,
            "stageVariables": null,
            "body": "Hello from Lambda!",
            "isBase64Encoded": false
          }""";

    @Test
    void multiValueHeadersShouldBeConsidered() throws IOException {
        executeTest("/duplicatedheaders", response -> {
            assertEquals(200, response.getStatusCode());
            assertEquals("""
        {"header2":["value1","value2"]}""", response.getBody());
        });
    }

    @Test
    void duplicateQueryStringsAreCombinedWithCommas () throws IOException {
        executeTest("/duplicateQueryStrings", response -> {
            assertEquals(200, response.getStatusCode());
            assertEquals("""
        {"parameter1":["value1","value2"]}""", response.getBody());
        });
    }

    @Test
    void singleValueQueryString() throws IOException {
        executeTest("/singleValueQueryString", response -> {
            assertEquals(200, response.getStatusCode());
            assertEquals("""
        {"parameter2":["value"]}""", response.getBody());
        });
    }

    private static void executeTest(String path, Consumer<APIGatewayProxyResponseEvent> responseConsumer) throws IOException {
        Map<String, Object> config = Map.of("micronaut.security.enabled", StringUtils.FALSE, "spec.name", "ApiGatewayProxyRequestEventFunctionTest");
        ApplicationContext ctx = ApplicationContext.builder().properties(config).build();
        try (ApiGatewayProxyRequestEventFunction handler = new ApiGatewayProxyRequestEventFunction(ctx)) {
            JsonMapper jsonMapper = ctx.getBean(JsonMapper.class);
            String json = PAYLOAD_V1_JSON.formatted(path);
            APIGatewayProxyRequestEvent input = jsonMapper.readValue(json, APIGatewayProxyRequestEvent.class);
            Context lambdaContext = new MockContext();
            APIGatewayProxyResponseEvent response = handler.handleRequest(input, lambdaContext);
            assertNotNull(response);
            responseConsumer.accept(response);
        }
    }

    @Requires(property = "spec.name", value = "ApiGatewayProxyRequestEventFunctionTest")
    @Controller
    static class DuplicatedHeadersController {

        @Post("/cookies")
        HttpResponse<?> cookies(HttpRequest<?> request) {
            MutableHttpResponse<?> response = HttpResponse.ok();
            Cookies cookies = request.getCookies();
            for (Cookie cookie : cookies.getAll()) {
                response.cookie(Cookie.of(cookie.getName(), cookie.getValue() == null ? "empty" : cookie.getValue()));
            }
            return response;
        }

        @Post("/duplicatedheaders")
        Map<String, Object> duplicatedHeaders(@Header List<String> header2) {
            return Map.of("header2", header2);
        }

        @Post("/duplicateQueryStrings")
        Map<String, Object> duplicateQueryStrings(@QueryValue List<String> parameter1) {
            return Map.of("parameter1", parameter1);
        }

        @Post("/singleValueQueryString")
        Map<String, Object> singleValueQueryString(@QueryValue List<String> parameter2) {
            return Map.of("parameter2", parameter2);
        }
    }
}
