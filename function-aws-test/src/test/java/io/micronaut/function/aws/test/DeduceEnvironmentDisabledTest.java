package io.micronaut.function.aws.test;

import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.function.aws.test.annotation.MicronautLambdaTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautLambdaTest(deduceEnvironment = false)
class DeduceEnvironmentDisabledTest {

    @Inject
    ApplicationContext context;

    @Test
    void deduceEnvironmentPropertyIsDisabled() {
        assertEquals(Optional.of(false), context.getProperty(Environment.DEDUCE_ENVIRONMENT_PROPERTY, Boolean.class));
    }
}
