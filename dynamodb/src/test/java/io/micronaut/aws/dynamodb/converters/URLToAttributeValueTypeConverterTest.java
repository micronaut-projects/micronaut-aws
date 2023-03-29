package io.micronaut.aws.dynamodb.converters;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class URLToAttributeValueTypeConverterTest {
    @Test
    void localDateTimeToAttributeValueTypeConverterTest(ConversionService conversionService) throws MalformedURLException {
        assertNotNull(conversionService);
        URL value = null;
        assertFalse(conversionService.convert(value, Argument.of(AttributeValue.class)).isPresent());
        value = new URL("https://micronaut.io");
        Optional<AttributeValue> attributeValueOptional = conversionService.convert(value, Argument.of(AttributeValue.class));
        assertTrue(attributeValueOptional.isPresent());
        AttributeValue attributeValue = attributeValueOptional.get();
        assertEquals("https://micronaut.io" ,attributeValue.s());
        assertEquals(AttributeValue.Type.S, attributeValue.type());

        Optional<URL> urlOptional = conversionService.convert(attributeValue, Argument.of(URL.class));
        assertTrue(urlOptional.isPresent());
        assertEquals(value, urlOptional.get());
    }
}
