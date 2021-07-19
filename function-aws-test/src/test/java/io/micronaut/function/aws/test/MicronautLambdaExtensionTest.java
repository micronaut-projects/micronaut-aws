package io.micronaut.function.aws.test;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.function.aws.test.annotation.MicronautLambdaTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@MicronautLambdaTest
public class MicronautLambdaExtensionTest {
    @Inject
    ApplicationContext context;

    @Test
    public void testContextProperlyConfigured() {
        Set<String> expectedNames = new HashSet<>(Arrays.asList("test", "function", "lambda"));
        assertNotNull(context);
        assertEquals(context.getEnvironment().getActiveNames(), expectedNames);
        Collection<BeanRegistration<EagerSingleton>> registrations =
                context.getActiveBeanRegistrations(EagerSingleton.class);
        assertEquals(1, registrations.size());
    }
}
