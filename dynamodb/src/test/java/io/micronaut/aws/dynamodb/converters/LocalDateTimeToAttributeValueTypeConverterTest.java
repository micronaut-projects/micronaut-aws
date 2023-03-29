package io.micronaut.aws.dynamodb.converters;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.Month;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class LocalDateTimeToAttributeValueTypeConverterTest {
    @Test
    void localDateTimeToAttributeValueTypeConverterTest(ConversionService conversionService) {
        assertNotNull(conversionService);
        LocalDateTime ld = null;
        assertFalse(conversionService.convert(ld, Argument.of(AttributeValue.class)).isPresent());
        ld = LocalDateTime.of(2023, Month.MARCH, 23, 18, 30, 15);
        Optional<AttributeValue> attributeValueOptional = conversionService.convert(ld, Argument.of(AttributeValue.class));
        assertTrue(attributeValueOptional.isPresent());
        AttributeValue attributeValue = attributeValueOptional.get();
        assertEquals("2023-03-23T18:30:15" ,attributeValue.s());
        assertEquals(AttributeValue.Type.S, attributeValue.type());

        Optional<LocalDateTime> localDateOptional = conversionService.convert(attributeValue, Argument.of(LocalDateTime.class));
        assertTrue(localDateOptional.isPresent());
        assertEquals(ld, localDateOptional.get());
    }
}
