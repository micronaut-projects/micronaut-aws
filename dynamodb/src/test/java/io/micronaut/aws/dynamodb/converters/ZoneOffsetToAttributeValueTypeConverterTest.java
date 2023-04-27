package io.micronaut.aws.dynamodb.converters;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.ZoneOffset;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class ZoneOffsetToAttributeValueTypeConverterTest {
    @Test
    void zoneIdToAttributeValueTypeConverterTest(ConversionService conversionService) {
        assertNotNull(conversionService);
        ZoneOffset value = null;
        assertFalse(conversionService.convert(value, Argument.of(AttributeValue.class)).isPresent());
        value = ZoneOffset.of("+1");
        Optional<AttributeValue> attributeValueOptional = conversionService.convert(value, Argument.of(AttributeValue.class));
        assertTrue(attributeValueOptional.isPresent());
        AttributeValue attributeValue = attributeValueOptional.get();
        assertEquals(AttributeValue.Type.S, attributeValue.type());
        assertEquals("+01:00" ,attributeValue.s());

        Optional<ZoneOffset> zoneIdOptional = conversionService.convert(attributeValue, Argument.of(ZoneOffset.class));
        assertTrue(zoneIdOptional.isPresent());
        assertEquals(ZoneOffset.of("+01:00"), zoneIdOptional.get());
    }
}
