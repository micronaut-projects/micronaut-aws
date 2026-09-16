package io.micronaut.docs.function.client.aws.atnamed

//tag::clazz[]
import io.micronaut.function.client.FunctionClient
import jakarta.inject.Named

@FunctionClient
internal interface AnalyticsClient {
    @Named("analytics") // <1>
    fun visit(productId: String): String
}
//end::clazz[]