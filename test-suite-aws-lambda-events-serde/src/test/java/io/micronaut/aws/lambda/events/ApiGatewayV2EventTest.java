package io.micronaut.aws.lambda.events;

import io.micronaut.function.aws.JsonMapperCustomPojoSerializer;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import java.io.File;
import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;

class ApiGatewayV2EventTest {

    @Disabled("Unexpected exception thrown: io.micronaut.function.aws.CustomPojoSerializerException: io.micronaut.serde.exceptions.SerdeException: Unable to deserialize type [RequestContext requestContext]: Null argument specified for [authorizer]. If this argument is allowed to be null annotate it with @Nullable")
    @Test
    void testDeserializationOfAPIGatewayV2HttpEvent() throws IOException {
        JsonMapperCustomPojoSerializer serializer = new JsonMapperCustomPojoSerializer();
        File f = new File("src/test/resources/apiGatewayV2HTTPEvent.json");
        assertTrue(f.exists());

        String json = FileUtils.text(f);
        assertNotNull(json);

        APIGatewayV2HTTPEvent event = assertDoesNotThrow(() -> serializer.fromJson(json, APIGatewayV2HTTPEvent.class));
        expectedEvent(event);
    }

    @Test
    void testDeserializationOfAPIGatewayV2HttpEventFromMicronautAwsLambdaEvents() throws IOException {
        JsonMapperCustomPojoSerializer serializer = new JsonMapperCustomPojoSerializer();
        File f = new File("src/test/resources/apiGatewayV2HTTPEvent.json");
        assertTrue(f.exists());

        String json = FileUtils.text(f);
        assertNotNull(json);

        io.micronaut.aws.lambda.events.APIGatewayV2HTTPEvent event = assertDoesNotThrow(() -> serializer.fromJson(json, io.micronaut.aws.lambda.events.APIGatewayV2HTTPEvent.class));
        expectedEvent(event);
    }

    void expectedEvent(io.micronaut.aws.lambda.events.APIGatewayV2HTTPEvent event) {
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

    void expectedEvent(APIGatewayV2HTTPEvent event) {
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
