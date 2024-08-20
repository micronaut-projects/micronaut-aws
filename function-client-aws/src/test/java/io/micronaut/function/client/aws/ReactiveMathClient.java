package io.micronaut.function.client.aws;

import io.micronaut.function.client.FunctionClient;
import jakarta.inject.Named;
import org.reactivestreams.Publisher;

@FunctionClient
interface ReactiveMathClient {
    Publisher<Long> max();

    @Named("round")
    Publisher<Integer> rnd(float value);

    Publisher<Long> sum(Suma sum);
}
