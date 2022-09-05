package io.micronaut.docs.function.aws;

import io.micronaut.context.ApplicationContext;
import io.micronaut.function.client.FunctionDefinition;
import io.micronaut.function.client.aws.AWSInvokeRequestDefinition;
import io.micronaut.function.client.aws.AwsInvokeRequestDefinition;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautTest(startApplication = false)
class AnalyticsClientTest {

    @Inject
    ApplicationContext applicationContext;

    @Test
    void testSetupFunctionDefinitions() {
        Collection<FunctionDefinition> definitions = applicationContext.getBeansOfType(FunctionDefinition.class);
        Collection<AWSInvokeRequestDefinition> awsV1Definition = getInstancesOf(definitions, AWSInvokeRequestDefinition.class);
        Collection<AwsInvokeRequestDefinition> awsV2Definition = getInstancesOf(definitions, AwsInvokeRequestDefinition.class);

        assertEquals(2, definitions.size());
        assertEquals(1, awsV1Definition.size());
        assertEquals(1, awsV2Definition.size());

        AWSInvokeRequestDefinition invokeRequestDefinition = awsV1Definition.iterator().next();

        assertEquals("analytics", invokeRequestDefinition.getName());
        //assertEquals("AwsLambdaFunctionName", invokeRequestDefinition.getInvokeRequest().getFunctionName());
    }

    <T extends FunctionDefinition> Collection<T> getInstancesOf(Collection<FunctionDefinition> defs, Class<T> tClass) {
        return defs.stream().filter(tClass::isInstance).map(tClass::cast).collect(Collectors.toList());
    }
}
