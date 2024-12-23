package io.micronaut.docs.function.aws

import io.micronaut.context.ApplicationContext
import io.micronaut.function.client.FunctionDefinition
import io.micronaut.function.client.aws.AWSInvokeRequestDefinition
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test

@MicronautTest(startApplication = false)
internal class AnalyticsClientTest {
    @Inject
    lateinit var applicationContext: ApplicationContext
    @Test
    fun testSetupFunctionDefinitions() {
        val definitions = applicationContext.getBeansOfType(FunctionDefinition::class.java)
        Assertions.assertEquals(1, definitions.size)
        Assertions.assertTrue(definitions.stream().findFirst().isPresent)
        Assertions.assertTrue(definitions.stream().findFirst().get() is AWSInvokeRequestDefinition)
        val invokeRequestDefinition = definitions.stream().findFirst().get() as AWSInvokeRequestDefinition
        Assertions.assertEquals("analytics", invokeRequestDefinition.name)
        //Assertions.assertEquals("AwsLambdaFunctionName", invokeRequestDefinition.invokeRequest.functionName)
    }
}