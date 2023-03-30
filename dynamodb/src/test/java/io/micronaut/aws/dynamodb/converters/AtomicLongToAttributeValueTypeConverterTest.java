package io.micronaut.aws.dynamodb.converters;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicLong;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class AtomicLongToAttributeValueTypeConverterTest {
    @Test
    void boolToAttributeValueTypeConverterTest(ConversionService conversionService) {
        assertNotNull(conversionService);
        AtomicLong value = null;
        assertFalse(conversionService.convert(value, Argument.of(AttributeValue.class)).isPresent());

        value = new AtomicLong(3L);
        assertTrue(conversionService.convert(value, Argument.of(AttributeValue.class)).isPresent());
        Optional<AttributeValue> attributeValueOptional = conversionService.convert(value, Argument.of(AttributeValue.class));
        assertTrue(attributeValueOptional.isPresent());
        AttributeValue attributeValue = attributeValueOptional.get();
        assertEquals(AttributeValue.Type.N, attributeValue.type());
        assertEquals("3", attributeValue.n());

        Optional<AtomicLong> valueOptional = conversionService.convert(attributeValue, Argument.of(AtomicLong.class));
        assertTrue(valueOptional.isPresent());
        assertEquals(3L, valueOptional.get().get());
    }
}
