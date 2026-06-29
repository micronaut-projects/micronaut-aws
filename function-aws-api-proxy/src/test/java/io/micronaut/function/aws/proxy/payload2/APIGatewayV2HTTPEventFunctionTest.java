package io.micronaut.function.aws.proxy.payload2;

import com.amazonaws.services.lambda.runtime.ClientContext;
import com.amazonaws.services.lambda.runtime.CognitoIdentity;
import com.amazonaws.services.lambda.runtime.Context;
import com.amazonaws.services.lambda.runtime.LambdaLogger;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.annotation.Requires;
import io.micronaut.core.util.StringUtils;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Header;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.QueryValue;
import io.micronaut.json.JsonMapper;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import static org.junit.jupiter.api.Assertions.*;

class APIGatewayV2HTTPEventFunctionTest {
    // https://docs.aws.amazon.com/apigateway/latest/developerguide/http-api-develop-integrations-lambda.html#http-api-develop-integrations-lambda.proxy-format
    private static String JSON = """
        {
          "version": "2.0",
          "routeKey": "$default",
          "rawPath": "%1$s",
          "rawQueryString": "parameter1=value1&parameter1=value2&parameter2=value",
          "cookies": [
            "cookie1",
            "cookie2"
          ],
          "headers": {
            "header1": "value1",
            "header2": "value1,value2"
          },
          "queryStringParameters": {
            "parameter1": "value1,value2",
            "parameter2": "value"
          },
          "requestContext": {
            "accountId": "123456789012",
            "apiId": "api-id",
            "authentication": {
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
            "authorizer": {
              "jwt": {
                "claims": {
                  "claim1": "value1",
                  "claim2": "value2"
                },
                "scopes": [
                  "scope1",
                  "scope2"
                ]
              }
            },
            "domainName": "id.execute-api.us-east-1.amazonaws.com",
            "domainPrefix": "id",
            "http": {
              "method": "POST",
              "path": "%1$s",
              "protocol": "HTTP/1.1",
              "sourceIp": "192.0.2.1",
              "userAgent": "agent"
            },
            "requestId": "id",
            "routeKey": "$default",
            "stage": "$default",
            "time": "12/Mar/2020:19:03:58 +0000",
            "timeEpoch": 1583348638390
          },
          "body": "Hello from Lambda",
          "pathParameters": {
            "parameter1": "value1"
          },
          "isBase64Encoded": false,
          "stageVariables": {
            "stageVariable1": "value1",
            "stageVariable2": "value2"
          }
        }""";

    @Test
    void duplicateHeadersAreCombinedWithCommas() throws IOException {
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

    private static void executeTest(String path, Consumer<APIGatewayV2HTTPResponse> responseConsumer) throws IOException {
        Map<String, Object> config = Map.of("micronaut.security.enabled", StringUtils.FALSE, "spec.name", "APIGatewayV2HTTPEventFunctionTest");
        ApplicationContext ctx = ApplicationContext.builder().properties(config).build();
        try (APIGatewayV2HTTPEventFunction handler = new APIGatewayV2HTTPEventFunction(ctx)) {
            JsonMapper jsonMapper = ctx.getBean(JsonMapper.class);
            String json = JSON.formatted(path);
            APIGatewayV2HTTPEvent input = jsonMapper.readValue(json, APIGatewayV2HTTPEvent.class);
            Context lambdaContext = mockContext();
            APIGatewayV2HTTPResponse response = handler.handleRequest(input, lambdaContext);
            assertNotNull(response);
            responseConsumer.accept(response);
        }
    }

    @Requires(property = "spec.name", value = "APIGatewayV2HTTPEventFunctionTest")
    @Controller
    static class DuplicatedHeadersController {

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

    private static Context mockContext() {
        return new Context() {

            @Override
            public String getAwsRequestId() {
                return null;
            }

            @Override
            public String getLogGroupName() {
                return null;
            }

            @Override
            public String getLogStreamName() {
                return null;
            }

            @Override
            public String getFunctionName() {
                return null;
            }

            @Override
            public String getFunctionVersion() {
                return null;
            }

            @Override
            public String getInvokedFunctionArn() {
                return null;
            }

            @Override
            public CognitoIdentity getIdentity() {
                return null;
            }

            @Override
            public ClientContext getClientContext() {
                return null;
            }

            @Override
            public int getRemainingTimeInMillis() {
                return 0;
            }

            @Override
            public int getMemoryLimitInMB() {
                return 0;
            }

            @Override
            public LambdaLogger getLogger() {
                return null;
            }
        };
    }

}
