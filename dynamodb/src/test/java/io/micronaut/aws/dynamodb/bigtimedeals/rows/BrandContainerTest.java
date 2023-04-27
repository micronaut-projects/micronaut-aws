package io.micronaut.aws.dynamodb.bigtimedeals.rows;

import io.micronaut.aws.dynamodb.DynamoDbConversionService;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import org.junit.jupiter.api.Test;
import software.amazon.awssdk.services.dynamodb.model.AttributeValue;

import java.util.Collections;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class BrandContainerTest {

    @Test
    void conversionServiceHandlesSet(DynamoDbConversionService dynamoDbConversionService) {
        BrandContainer brandContainer = new BrandContainer(BrandContainer.KEY.getPartionKey(),
            BrandContainer.KEY.getSortKey(),
            BrandContainer.class.getName(),
            Collections.singleton("Nike"));
        Map<String, AttributeValue> m = dynamoDbConversionService.convert(brandContainer);
        assertNotNull(m);
        assertTrue(m.containsKey("pk"));
        assertEquals("BRANDS", m.get("pk").s());
        assertTrue(m.containsKey("sk"));
        assertEquals("BRANDS", m.get("pk").s());
        assertTrue(m.containsKey("className"));
        assertEquals("io.micronaut.aws.dynamodb.bigtimedeals.rows.BrandContainer", m.get("className").s());
        assertTrue(m.containsKey("brands"));
        assertEquals(Collections.singletonList("Nike"), m.get("brands").ss());

        BrandContainer result = dynamoDbConversionService.convert(m, BrandContainer.class);
        assertEquals("BRANDS", result.getPk());
        assertEquals("BRANDS", result.getSk());
        assertEquals("io.micronaut.aws.dynamodb.bigtimedeals.rows.BrandContainer", result.getClassName());
        assertEquals(Collections.singleton("Nike"), result.getBrands());
    }
}
