package io.micronaut.aws.lambda.events.tests;

import io.micronaut.aws.lambda.events.FileUtils;
import io.micronaut.function.aws.JsonMapperCustomPojoSerializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import com.amazonaws.services.lambda.runtime.events.SQSEvent;

import static org.junit.jupiter.api.Assertions.*;

class SqSEventTest {

    @Test
    void testDeserializationOfSqSEvent() throws IOException {
        JsonMapperCustomPojoSerializer serializer = new JsonMapperCustomPojoSerializer();
        String json = FileUtils.text(this.getClass().getClassLoader(), "sqs-event.json").orElse(null);
        assertNotNull(json);
        SQSEvent event = assertDoesNotThrow(() -> serializer.fromJson(json, SQSEvent.class));
        assertNotNull(event);
        assertNotNull(event.getRecords());
        assertEquals(1, event.getRecords().size());
    }
}
