package io.micronaut.aws.lambda.events.tests;

import com.amazonaws.services.lambda.runtime.CustomPojoSerializer;
import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerRequestEvent;
import io.micronaut.aws.lambda.events.FileUtils;
import io.micronaut.function.aws.JsonMapperCustomPojoSerializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class ApplicationLoadBalancerRequestEventTest {

    @Test
    @SuppressWarnings("java:S1313") // IP usage is safe here
    void testDeserializationOfApplicationLoadBalancerRequestEvent() throws IOException {
        String json = FileUtils.text(this.getClass().getClassLoader(), "albRequest.json").orElse(null);
        assertNotNull(json);
        ApplicationLoadBalancerRequestEvent event = assertDoesNotThrow(() -> CustomPojoSerializerUtils.serializeFromJson(json, ApplicationLoadBalancerRequestEvent.class));
        assertEquals("arn:aws:elasticloadbalancing:us-east-1:646307737039:targetgroup/fubar-dev-tg/97533b9b279f7d7f", event.getRequestContext().getElb().getTargetGroupArn());
        assertEquals("GET", event.getHttpMethod());
        assertEquals("/", event.getPath());
        assertEquals(Collections.emptyMap(), event.getQueryStringParameters());

        assertEquals("*/*", event.getHeaders().get("accept"));
        assertEquals("fubar-dev-alb-376753725.us-east-1.elb.amazonaws.com", event.getHeaders().get("host"));
        assertEquals("curl/7.88.1", event.getHeaders().get("user-agent"));
        assertEquals("Root=1-649c0255-6789eb512889f9cd6bc9193c", event.getHeaders().get("x-amzn-trace-id"));
        assertEquals("80.26.234.66", event.getHeaders().get("x-forwarded-for"));
        assertEquals("80", event.getHeaders().get("x-forwarded-port"));
        assertEquals("http", event.getHeaders().get("x-forwarded-proto"));
        assertEquals("", event.getBody());
        assertFalse(event.getIsBase64Encoded());
    }
}
