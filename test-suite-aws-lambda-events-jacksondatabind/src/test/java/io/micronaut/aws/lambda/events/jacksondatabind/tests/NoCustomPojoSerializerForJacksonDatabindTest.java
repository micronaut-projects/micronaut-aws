package io.micronaut.aws.lambda.events.jacksondatabind.tests;

import org.junit.jupiter.api.Test;

import static io.micronaut.aws.lambda.events.tests.CustomPojoSerializerUtils.loadSerializer;
import static org.junit.jupiter.api.Assertions.assertNull;

class NoCustomPojoSerializerForJacksonDatabindTest {
    @Test
    void noCustomPojoSerializerForJacksonDatabindTest() {
        assertNull(loadSerializer());
    }
}
