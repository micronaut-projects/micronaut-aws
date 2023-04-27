package io.micronaut.aws.dynamodb.converters;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class LocalTimeToAttributeValueTypeConverterTest {
    @Test
    void localDateTimeToAttributeValueTypeConverterTest(ConversionService conversionService) {
        assertNotNull(conversionService);
        LocalTime ld = null;
        assertFalse(conversionService.convert(ld, Argument.of(AttributeValue.class)).isPresent());
        ld = LocalTime.parse("10:15:30");
        Optional<AttributeValue> attributeValueOptional = conversionService.convert(ld, Argument.of(AttributeValue.class));
        assertTrue(attributeValueOptional.isPresent());
        AttributeValue attributeValue = attributeValueOptional.get();
        assertEquals("10:15:30" ,attributeValue.s());
        assertEquals(AttributeValue.Type.S, attributeValue.type());

        Optional<LocalTime> localTimeOptional = conversionService.convert(attributeValue, Argument.of(LocalTime.class));
        assertTrue(localTimeOptional.isPresent());
        assertEquals(ld, localTimeOptional.get());
    }
}
