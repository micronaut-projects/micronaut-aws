package io.micronaut.aws.lambda.events.tests;

import com.amazonaws.services.lambda.runtime.events.ApplicationLoadBalancerResponseEvent;
import io.micronaut.aws.lambda.events.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ApplicationLoadBalancerResponseEventTest {

    @Test
    void testDeserializationOfApplicationLoadBalancerResponseEvent() throws IOException {
        String json = FileUtils.text(this.getClass().getClassLoader(), "albResponse.json").orElse(null);
        assertNotNull(json);
        ApplicationLoadBalancerResponseEvent event = assertDoesNotThrow(() -> CustomPojoSerializerUtils.serializeFromJson(json, ApplicationLoadBalancerResponseEvent.class));
        assertEquals(200, event.getStatusCode());
        assertNull(event.getStatusDescription());
        assertFalse(event.getIsBase64Encoded());
        assertEquals("Wed, 28 Jun 2023 09:50:16 GMT", event.getHeaders().get("Date"));
        assertEquals("application/json", event.getHeaders().get("Content-Type"));
        assertEquals(Collections.singletonList("Wed, 28 Jun 2023 09:50:16 GMT"), event.getMultiValueHeaders().get("Date"));
        assertEquals(Collections.singletonList("application/json"), event.getMultiValueHeaders().get("Content-Type"));
        assertEquals("{\"message\":\"Will it Blend?\"}", event.getBody());
    }
}
