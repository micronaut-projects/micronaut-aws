package io.micronaut.aws.lambda.events.serde.tests;

import org.junit.jupiter.api.Test;

import static io.micronaut.aws.lambda.events.tests.CustomPojoSerializerUtils.loadSerializer;
import static org.junit.jupiter.api.Assertions.assertNotNull;

class CustomPojoSerializerForSerdeTest {
    @Test
    void customPojoSerializerForSerdeTest() {
        assertNotNull(loadSerializer());
    }
}
