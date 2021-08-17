package io.micronaut.docs.function.client.aws.methodnamed

import io.micronaut.function.client.FunctionClient

@FunctionClient
internal interface AnalyticsClient {
    fun analytics(productId: String): String
}