package io.micronaut.docs.function.aws

import io.micronaut.context.ApplicationContext
import io.micronaut.function.client.FunctionDefinition
import io.micronaut.function.client.aws.AWSInvokeRequestDefinition
import io.micronaut.function.client.aws.AwsInvokeRequestDefinition
import io.micronaut.test.extensions.junit5.annotation.MicronautTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import kotlin.streams.toList

@MicronautTest(startApplication = false)
internal class AnalyticsClientTest {

    @Inject
    lateinit var applicationContext: ApplicationContext

    @Test
    fun testSetupFunctionDefinitions() {
        val definitions = applicationContext.getBeansOfType(FunctionDefinition::class.java)
        Assertions.assertEquals(2, definitions.size)

        val awsV1Definition = getInstancesOf(definitions, AWSInvokeRequestDefinition::class.java)
        val awsV2Definition = getInstancesOf(definitions, AwsInvokeRequestDefinition::class.java)

        Assertions.assertEquals(1, awsV1Definition.size)
        Assertions.assertEquals(1, awsV2Definition.size)

        val invokeRequestDefinition = awsV1Definition.first()
        Assertions.assertEquals("analytics", invokeRequestDefinition.name)
        //Assertions.assertEquals("AwsLambdaFunctionName", invokeRequestDefinition.invokeRequest.functionName)
    }

    fun <T : FunctionDefinition> getInstancesOf(defs: Collection<FunctionDefinition>, tClass: Class<T>): Collection<T> {
        return defs.stream().filter { tClass.isInstance(it) }.map { tClass.cast(it) }.toList()
    }
}
