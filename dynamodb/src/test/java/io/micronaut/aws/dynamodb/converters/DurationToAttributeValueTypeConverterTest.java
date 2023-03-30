package io.micronaut.aws.dynamodb.converters;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Duration;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class DurationToAttributeValueTypeConverterTest {
    @Test
    void doubleToAttributeValueTypeConverterTest(ConversionService conversionService) {
        assertNotNull(conversionService);
        Duration val = null;
        assertFalse(conversionService.convert(val, Argument.of(AttributeValue.class)).isPresent());
        for (TestRun testRun : Arrays.asList(new TestRun(Duration.ofDays(1), "86400"),
                new TestRun(Duration.ofSeconds(9), "9"),
                new TestRun(Duration.ofSeconds(-9), "-9"),
                new TestRun(Duration.ofNanos(1_234_567_890), "1.234567890"),
                new TestRun(Duration.ofMillis(1), "0.001000000"),
                new TestRun(Duration.ofNanos(1), "0.000000001")
        )) {
            val = testRun.getDuration();
            assertTrue(conversionService.convert(val, Argument.of(AttributeValue.class)).isPresent());
            Optional<AttributeValue> attributeValueOptional = conversionService.convert(val, Argument.of(AttributeValue.class));
            assertTrue(attributeValueOptional.isPresent());
            AttributeValue attributeValue = attributeValueOptional.get();
            assertNotNull(attributeValue.n());
            assertEquals(testRun.getResult(), attributeValue.n());
            assertEquals(AttributeValue.Type.N, attributeValue.type());

            Optional<Duration> valueOptional = conversionService.convert(attributeValue, Argument.of(Duration.class));
            assertTrue(valueOptional.isPresent());
            assertEquals(val, valueOptional.get());
        }
    }

    static class TestRun {
        private final Duration duration;
        private final String result;

        public TestRun(Duration duration, String result) {
            this.duration = duration;
            this.result = result;
        }

        public Duration getDuration() {
            return duration;
        }

        public String getResult() {
            return result;
        }
    }
}
