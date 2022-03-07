package io.micronaut.docs.function.aws;

import io.micronaut.context.ApplicationContext;
import io.micronaut.function.client.FunctionDefinition;
import io.micronaut.function.client.aws.AWSInvokeRequestDefinition;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Collection;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

@MicronautTest(startApplication = false)
class AnalyticsClientTest {
    @Inject
    ApplicationContext applicationContext;

    @Test
    void testSetupFunctionDefinitions() {
        Collection<FunctionDefinition> definitions = applicationContext.getBeansOfType(FunctionDefinition.class);

        assertEquals(1, definitions.size());
        assertTrue(definitions.stream().findFirst().isPresent());
        assertTrue(definitions.stream().findFirst().get() instanceof AWSInvokeRequestDefinition);

        AWSInvokeRequestDefinition invokeRequestDefinition = (AWSInvokeRequestDefinition) definitions.stream().findFirst().get();

        assertEquals("analytics", invokeRequestDefinition.getName());
        //assertEquals("AwsLambdaFunctionName", invokeRequestDefinition.getInvokeRequest().getFunctionName());
    }
}
