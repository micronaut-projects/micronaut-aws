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
class CharSequenceToAttributeValueTypeConverterTest {
    @Test
    void charSequenceToAttributeValueTypeConverterTest(ConversionService conversionService) {
        assertNotNull(conversionService);
        CharSequence cs = null;
        assertFalse(conversionService.convert(cs, Argument.of(AttributeValue.class)).isPresent());
        cs = "";
        assertFalse(conversionService.convert(cs, Argument.of(AttributeValue.class)).isPresent());
        cs = "foo";
        Optional<AttributeValue> attributeValueOptional = conversionService.convert(cs, Argument.of(AttributeValue.class));
        assertTrue(attributeValueOptional.isPresent());
        AttributeValue attributeValue = attributeValueOptional.get();
        assertEquals("foo" ,attributeValue.s());
        assertEquals(AttributeValue.Type.S, attributeValue.type());

        Optional<CharSequence> charSequenceOptional = conversionService.convert(attributeValue, Argument.of(CharSequence.class));
        assertTrue(charSequenceOptional.isPresent());
        assertEquals("foo", charSequenceOptional.get());
    }
}
