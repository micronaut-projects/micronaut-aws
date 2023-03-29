package io.micronaut.aws.dynamodb.converters;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.MonthDay;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class MonthDayToAttributeValueTypeConverterTest {
    @Test
    void charSequenceToAttributeValueTypeConverterTest(ConversionService conversionService) {
        assertNotNull(conversionService);
        MonthDay value = null;
        assertFalse(conversionService.convert(value, Argument.of(AttributeValue.class)).isPresent());
        value = MonthDay.of(5, 21);
        Optional<AttributeValue> attributeValueOptional = conversionService.convert(value, Argument.of(AttributeValue.class));
        assertTrue(attributeValueOptional.isPresent());
        AttributeValue attributeValue = attributeValueOptional.get();
        assertEquals("--05-21" ,attributeValue.s());
        assertEquals(AttributeValue.Type.S, attributeValue.type());

        Optional<MonthDay> monthDayOptional = conversionService.convert(attributeValue, Argument.of(MonthDay.class));
        assertTrue(monthDayOptional.isPresent());
        assertEquals(value, monthDayOptional.get());
    }
}
