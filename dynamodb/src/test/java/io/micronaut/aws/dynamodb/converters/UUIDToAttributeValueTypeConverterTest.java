package io.micronaut.aws.dynamodb.converters;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


@MicronautTest(startApplication = false)
class UUIDToAttributeValueTypeConverterTest {
    @Test
    void charSequenceToAttributeValueTypeConverterTest(ConversionService conversionService) {
        assertNotNull(conversionService);
        UUID uuid = null;
        assertFalse(conversionService.convert(uuid, Argument.of(AttributeValue.class)).isPresent());
        uuid = UUID.randomUUID();
        Optional<AttributeValue> attributeValueOptional = conversionService.convert(uuid, Argument.of(AttributeValue.class));
        assertTrue(attributeValueOptional.isPresent());
        AttributeValue attributeValue = attributeValueOptional.get();
        assertEquals(uuid.toString() ,attributeValue.s());
        assertEquals(AttributeValue.Type.S, attributeValue.type());

        Optional<UUID> uuidOptional = conversionService.convert(attributeValue, Argument.of(UUID.class));
        assertTrue(uuidOptional.isPresent());
        assertEquals(uuid, uuidOptional.get());
    }
}
