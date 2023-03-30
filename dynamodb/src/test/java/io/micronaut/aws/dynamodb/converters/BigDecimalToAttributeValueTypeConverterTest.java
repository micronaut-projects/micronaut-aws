package io.micronaut.aws.dynamodb.converters;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class BigDecimalToAttributeValueTypeConverterTest {
    @Test
    void bigDecimalToAttributeValueTypeConverterTest(ConversionService conversionService) {
        assertNotNull(conversionService);
        BigDecimal val = null;
        assertFalse(conversionService.convert(val, Argument.of(AttributeValue.class)).isPresent());

        val = new BigDecimal("10.5");
        assertTrue(conversionService.convert(val, Argument.of(AttributeValue.class)).isPresent());
        Optional<AttributeValue> attributeValueOptional = conversionService.convert(val, Argument.of(AttributeValue.class));
        assertTrue(attributeValueOptional.isPresent());
        AttributeValue attributeValue = attributeValueOptional.get();
        assertNotNull(attributeValue.n());
        assertEquals(AttributeValue.Type.N, attributeValue.type());

        Optional<BigDecimal> numberOptional = conversionService.convert(attributeValue, Argument.of(BigDecimal.class));
        assertTrue(numberOptional.isPresent());
        assertEquals(val, numberOptional.get());
    }
}
