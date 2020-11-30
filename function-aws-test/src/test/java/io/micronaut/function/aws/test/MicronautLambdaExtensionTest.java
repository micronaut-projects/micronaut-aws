package io.micronaut.function.aws.test;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.BeanRegistration;
import io.micronaut.function.aws.test.annotation.MicronautLambdaTest;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.inject.Inject;
import java.util.*;

@MicronautLambdaTest
public class MicronautLambdaExtensionTest {
    @Inject
    ApplicationContext context;

    @Test
    public void testContextProperlyConfigured() {
        Set<String> expectedNames = new HashSet<>(Arrays.asList("test", "function", "lambda"));
        Assertions.assertNotNull(context);
        Assertions.assertEquals(context.getEnvironment().getActiveNames(), expectedNames);
        Collection<BeanRegistration<EagerSingleton>> registrations =
                context.getActiveBeanRegistrations(EagerSingleton.class);
        Assertions.assertEquals(1, registrations.size());
    }
}
