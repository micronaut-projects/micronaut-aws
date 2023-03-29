package io.micronaut.aws.dynamodb.converters;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class AtomicBooleanToAttributeValueTypeConverterTest {
    @Test
    void boolToAttributeValueTypeConverterTest(ConversionService conversionService) {
        assertNotNull(conversionService);
        AtomicBoolean bool = null;
        assertFalse(conversionService.convert(bool, Argument.of(AttributeValue.class)).isPresent());

        bool = new AtomicBoolean(false);
        assertTrue(conversionService.convert(bool, Argument.of(AttributeValue.class)).isPresent());
        Optional<AttributeValue> attributeValueOptional = conversionService.convert(bool, Argument.of(AttributeValue.class));
        assertTrue(attributeValueOptional.isPresent());
        AttributeValue attributeValue = attributeValueOptional.get();
        assertFalse(attributeValue.bool());
        assertEquals(AttributeValue.Type.BOOL, attributeValue.type());

        Optional<AtomicBoolean> booleanOptional = conversionService.convert(attributeValue, Argument.of(AtomicBoolean.class));
        assertTrue(booleanOptional.isPresent());
        assertFalse(booleanOptional.get().get());

        bool = new AtomicBoolean(true);
        assertTrue(conversionService.convert(bool, Argument.of(AttributeValue.class)).isPresent());
        attributeValueOptional = conversionService.convert(bool, Argument.of(AttributeValue.class));
        assertTrue(attributeValueOptional.isPresent());
        attributeValue = attributeValueOptional.get();
        assertEquals(AttributeValue.Type.BOOL, attributeValue.type());
        assertTrue(attributeValue.bool());

        booleanOptional = conversionService.convert(attributeValue, Argument.of(AtomicBoolean.class));
        assertTrue(booleanOptional.isPresent());
        assertTrue(booleanOptional.get().get());
    }
}
