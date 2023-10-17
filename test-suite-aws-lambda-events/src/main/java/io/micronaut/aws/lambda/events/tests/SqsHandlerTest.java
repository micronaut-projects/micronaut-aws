package io.micronaut.aws.lambda.events.tests;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;
import io.micronaut.aws.lambda.events.FileUtils;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class SqsHandlerTest {
    @Test
    void testDeserializationOfApplicationLoadBalancerRequestEvent() throws IOException {
        String json = FileUtils.text(this.getClass().getClassLoader(), "sqs-event.json").orElse(null);
        assertNotNull(json);
        SQSEvent event = assertDoesNotThrow(() -> CustomPojoSerializerUtils.serializeFromJson(json, SQSEvent.class));
        assertNotNull(event);
        assertNotNull(event.getRecords());
        assertEquals(1, event.getRecords().size());
    }
}
