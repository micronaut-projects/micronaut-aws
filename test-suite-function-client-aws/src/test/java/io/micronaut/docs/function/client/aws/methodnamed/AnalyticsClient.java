package io.micronaut.docs.function.client.aws.methodnamed;

import io.micronaut.function.client.FunctionClient;

@FunctionClient
public interface AnalyticsClient {
    String analytics(String productId);
}
