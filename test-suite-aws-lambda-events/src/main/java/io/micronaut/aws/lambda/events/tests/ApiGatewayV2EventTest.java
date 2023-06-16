package io.micronaut.aws.lambda.events.tests;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import io.micronaut.aws.lambda.events.FileUtils;
import io.micronaut.function.aws.JsonMapperCustomPojoSerializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiGatewayV2EventTest {

    @Test
    void testDeserializationOfAPIGatewayV2HttpEvent() throws IOException {
        JsonMapperCustomPojoSerializer serializer = new JsonMapperCustomPojoSerializer();
        String json = FileUtils.text(this.getClass().getClassLoader(), "apiGatewayV2HTTPEvent.json").orElse(null);
        assertNotNull(json);
        APIGatewayV2HTTPEvent event = assertDoesNotThrow(() -> serializer.fromJson(json, APIGatewayV2HTTPEvent.class));
        expectedEvent(event);
    }

    public static void expectedEvent(APIGatewayV2HTTPEvent event) {
        assertEquals("2.0",event.getVersion());
        assertEquals("$default",event.getRouteKey());
        assertEquals("/",event.getRawPath());
        assertEquals("",event.getRawQueryString());
        assertEquals("*/*",event.getHeaders().get("accept"));
        assertEquals("0",event.getHeaders().get("content-length"));
        assertEquals("y9j2vzg784.execute-api.us-east-1.amazonaws.com",event.getHeaders().get("host"));
        assertEquals("curl/7.88.1",event.getHeaders().get("user-agent"));
        assertEquals("Root=1-6488b8c4-28a476ca7ae7e1447b5e2d8f",event.getHeaders().get("x-amzn-trace-id"));
        assertEquals("80.26.234.66",event.getHeaders().get("x-forwarded-for"));
        assertEquals("443",event.getHeaders().get("x-forwarded-port"));
        assertEquals("https",event.getHeaders().get("x-forwarded-proto"));
        assertEquals("646406757139",event.getRequestContext().getAccountId());
        assertEquals("y9j2zvz764",event.getRequestContext().getApiId());
        assertEquals("y5j3zzg784.execute-api.us-east-1.amazonaws.com",event.getRequestContext().getDomainName());
        assertEquals("y5j3zzg784",event.getRequestContext().getDomainPrefix());
        assertEquals("GET",event.getRequestContext().getHttp().getMethod());
        assertEquals("/",event.getRequestContext().getHttp().getPath());
        assertEquals("HTTP/1.1",event.getRequestContext().getHttp().getProtocol());
        assertEquals("80.26.234.66",event.getRequestContext().getHttp().getSourceIp());
        assertEquals("curl/7.88.1",event.getRequestContext().getHttp().getUserAgent());
        assertEquals("GeHOyjS9oAMEV-A=",event.getRequestContext().getRequestId());
        assertEquals("$default",event.getRequestContext().getRouteKey());
        assertEquals("$default",event.getRequestContext().getStage());
        assertEquals("13/Jun/2023:18:43:16 +0000",event.getRequestContext().getTime());
        assertEquals(1686681796799L ,event.getRequestContext().getTimeEpoch());
        assertFalse(event.getIsBase64Encoded());
    }
}
