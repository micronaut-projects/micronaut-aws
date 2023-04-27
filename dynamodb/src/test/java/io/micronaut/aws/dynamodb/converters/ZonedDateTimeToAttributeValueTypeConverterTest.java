package io.micronaut.aws.dynamodb.converters;

import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.type.Argument;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class ZonedDateTimeToAttributeValueTypeConverterTest {
    @Test
    void localDateTimeToAttributeValueTypeConverterTest(ConversionService conversionService) {
        assertNotNull(conversionService);
        ZonedDateTime ld = null;
        assertFalse(conversionService.convert(ld, Argument.of(AttributeValue.class)).isPresent());
        ld = Instant.EPOCH.atZone(ZoneId.of("Europe/Paris"));
        Optional<AttributeValue> attributeValueOptional = conversionService.convert(ld, Argument.of(AttributeValue.class));
        assertTrue(attributeValueOptional.isPresent());
        AttributeValue attributeValue = attributeValueOptional.get();
        assertEquals("1970-01-01T01:00+01:00[Europe/Paris]" ,attributeValue.s());
        assertEquals(AttributeValue.Type.S, attributeValue.type());

        Optional<ZonedDateTime> localDateOptional = conversionService.convert(attributeValue, Argument.of(ZonedDateTime.class));
        assertTrue(localDateOptional.isPresent());
        assertEquals(ld, localDateOptional.get());
    }
}
