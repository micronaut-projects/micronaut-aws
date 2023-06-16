package io.micronaut.aws.lambda.events.tests;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import io.micronaut.aws.lambda.events.APIGatewayV2HttpEventUtils;
import io.micronaut.aws.lambda.events.FileUtils;
import io.micronaut.function.aws.JsonMapperCustomPojoSerializer;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class ApiGatewayV2EventTest {

    @Test
    void testDeserializationOfAPIGatewayV2HttpEvent() throws IOException {
        JsonMapperCustomPojoSerializer serializer = new JsonMapperCustomPojoSerializer();
        String json = FileUtils.text(this.getClass().getClassLoader(), "apiGatewayV2HTTPEvent.json").orElse(null);
        assertNotNull(json);
        APIGatewayV2HTTPEvent event = assertDoesNotThrow(() -> serializer.fromJson(json, APIGatewayV2HTTPEvent.class));
        APIGatewayV2HttpEventUtils.expectedEvent(event);
    }
}
