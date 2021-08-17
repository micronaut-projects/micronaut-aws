package io.micronaut.docs.function.client.aws.atnamed;

//tag::clazz[]
import io.micronaut.function.client.FunctionClient;
import jakarta.inject.Named;
@FunctionClient
public interface AnalyticsClient {

    @Named("analytics") // <1>
    String visit(String productId);
}
//end::clazz[]
