package io.micronaut.docs.function.client.aws.atnamed

import io.micronaut.function.client.FunctionClient
import jakarta.inject.Named
@FunctionClient
interface AnalyticsClient {
    @Named('analytics')
    String visit(String productId);
}
