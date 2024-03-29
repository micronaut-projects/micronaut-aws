package io.micronaut.function.aws.test;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.DefaultApplicationContextBuilder;
import io.micronaut.function.aws.test.annotation.MicronautLambdaTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautLambdaTest(contextBuilder = CustomBuilderTest.CustomBuilder.class)
class CustomBuilderTest {

    @Inject
    ApplicationContext context;

    @Test
    void testContextProperlyConfigured() {
        Set<String> expectedNames = new HashSet<>(Arrays.asList("test", "function", "lambda", "custom-env"));
        assertEquals(context.getEnvironment().getActiveNames(), expectedNames);
        assertEquals(Optional.of("baz"), context.getProperty("test.first", String.class));
        assertEquals(Optional.of("bar"), context.getProperty("test.second", String.class));
    }

    public static class CustomBuilder extends DefaultApplicationContextBuilder {
        public CustomBuilder() {
            environments("custom-env");
            Map<String, Object> overrides = new HashMap<>();
            overrides.put("test.first", "baz");
            properties(overrides);
        }
    }
}
