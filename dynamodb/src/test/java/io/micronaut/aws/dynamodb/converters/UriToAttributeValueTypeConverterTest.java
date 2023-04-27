package io.micronaut.aws.dynamodb.converters;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.net.MalformedURLException;
import java.util.Optional;
import java.net.URI;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class UriToAttributeValueTypeConverterTest {
    @Test
    void uriToAttributeValueTypeConverterTest(ConversionService conversionService) throws MalformedURLException {
        assertNotNull(conversionService);
        URI value = null;
        assertFalse(conversionService.convert(value, Argument.of(AttributeValue.class)).isPresent());
        value = URI.create("https://micronaut.io");
        Optional<AttributeValue> attributeValueOptional = conversionService.convert(value, Argument.of(AttributeValue.class));
        assertTrue(attributeValueOptional.isPresent());
        AttributeValue attributeValue = attributeValueOptional.get();
        assertEquals("https://micronaut.io" ,attributeValue.s());
        assertEquals(AttributeValue.Type.S, attributeValue.type());

        Optional<URI> urlOptional = conversionService.convert(attributeValue, Argument.of(URI.class));
        assertTrue(urlOptional.isPresent());
        assertEquals(value, urlOptional.get());
    }
}
