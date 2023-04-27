package io.micronaut.aws.dynamodb.converters;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class LongToAttributeValueTypeConverterTest {
    @Test
    void longToAttributeValueTypeConverterTest(ConversionService conversionService) {
        assertNotNull(conversionService);
        Long val = null;
        assertFalse(conversionService.convert(val, Argument.of(AttributeValue.class)).isPresent());

        val = Long.valueOf("10");
        assertTrue(conversionService.convert(val, Argument.of(AttributeValue.class)).isPresent());
        Optional<AttributeValue> attributeValueOptional = conversionService.convert(val, Argument.of(AttributeValue.class));
        assertTrue(attributeValueOptional.isPresent());
        AttributeValue attributeValue = attributeValueOptional.get();
        assertNotNull(attributeValue.n());
        assertEquals(AttributeValue.Type.N, attributeValue.type());

        Optional<Long> numberOptional = conversionService.convert(attributeValue, Argument.of(Long.class));
        assertTrue(numberOptional.isPresent());
        assertEquals(10, numberOptional.get());
    }
}
