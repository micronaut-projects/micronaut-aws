package io.micronaut.function.client.aws;

import io.micronaut.function.client.FunctionClient;
import jakarta.inject.Named;

@FunctionClient
interface MathClient {
    Long max();

    @Named("round")
    int rnd(float value);

    long sum(Suma sum);
}
