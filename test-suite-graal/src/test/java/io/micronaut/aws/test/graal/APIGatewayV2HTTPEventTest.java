package io.micronaut.aws.test.graal;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import io.micronaut.function.aws.JsonMapperCustomPojoSerializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

public class APIGatewayV2HTTPEventTest {

    @SuppressWarnings("java:S1313")
    public static void expectedEvent(APIGatewayV2HTTPEvent event) {
        assertEquals("2.0", event.getVersion());
        assertEquals("$default", event.getRouteKey());
        assertEquals("/", event.getRawPath());
        assertEquals("", event.getRawQueryString());
        assertEquals("*/*", event.getHeaders().get("accept"));
        assertEquals("0", event.getHeaders().get("content-length"));
        assertEquals("y9j2vzg784.execute-api.us-east-1.amazonaws.com", event.getHeaders().get("host"));
        assertEquals("curl/7.88.1", event.getHeaders().get("user-agent"));
        assertEquals("Root=1-6488b8c4-28a476ca7ae7e1447b5e2d8f", event.getHeaders().get("x-amzn-trace-id"));
        assertEquals("80.26.234.66", event.getHeaders().get("x-forwarded-for"));
        assertEquals("443", event.getHeaders().get("x-forwarded-port"));
        assertEquals("https", event.getHeaders().get("x-forwarded-proto"));
        assertEquals("646406757139", event.getRequestContext().getAccountId());
        assertEquals("y9j2zvz764", event.getRequestContext().getApiId());
        assertEquals("y5j3zzg784.execute-api.us-east-1.amazonaws.com", event.getRequestContext().getDomainName());
        assertEquals("y5j3zzg784", event.getRequestContext().getDomainPrefix());
        assertEquals("GET", event.getRequestContext().getHttp().getMethod());
        assertEquals("/", event.getRequestContext().getHttp().getPath());
        assertEquals("HTTP/1.1", event.getRequestContext().getHttp().getProtocol());
        assertEquals("80.26.234.66", event.getRequestContext().getHttp().getSourceIp());
        assertEquals("curl/7.88.1", event.getRequestContext().getHttp().getUserAgent());
        assertEquals("GeHOyjS9oAMEV-A=", event.getRequestContext().getRequestId());
        assertEquals("$default", event.getRequestContext().getRouteKey());
        assertEquals("$default", event.getRequestContext().getStage());
        assertEquals("13/Jun/2023:18:43:16 +0000", event.getRequestContext().getTime());
        assertEquals(1686681796799L, event.getRequestContext().getTimeEpoch());
        assertFalse(event.getIsBase64Encoded());
    }

    @Test
    void testDeserializationOfAPIGatewayV2HttpEvent() throws IOException {
        JsonMapperCustomPojoSerializer serializer = new JsonMapperCustomPojoSerializer();
        String json = """
{
  "version": "2.0",
  "routeKey": "$default",
  "rawPath": "/",
  "rawQueryString": "",
  "headers": {
    "accept": "*/*",
    "content-length": "0",
    "host": "y9j2vzg784.execute-api.us-east-1.amazonaws.com",
    "user-agent": "curl/7.88.1",
    "x-amzn-trace-id": "Root=1-6488b8c4-28a476ca7ae7e1447b5e2d8f",
    "x-forwarded-for": "80.26.234.66",
    "x-forwarded-port": "443",
    "x-forwarded-proto": "https"
  },
  "requestContext": {
    "accountId": "646406757139",
    "apiId": "y9j2zvz764",
    "domainName": "y5j3zzg784.execute-api.us-east-1.amazonaws.com",
    "domainPrefix": "y5j3zzg784",
    "http": {
      "method": "GET",
      "path": "/",
      "protocol": "HTTP/1.1",
      "sourceIp": "80.26.234.66",
      "userAgent": "curl/7.88.1"
    },
    "requestId": "GeHOyjS9oAMEV-A=",
    "routeKey": "$default",
    "stage": "$default",
    "time": "13/Jun/2023:18:43:16 +0000",
    "timeEpoch": 1686681796799
  },
  "isBase64Encoded": false
}""";
        assertNotNull(json);
        APIGatewayV2HTTPEvent event = assertDoesNotThrow(() -> serializer.fromJson(json, APIGatewayV2HTTPEvent.class));
        expectedEvent(event);
    }
}
