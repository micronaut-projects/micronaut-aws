package io.micronaut.function.aws;
// tag::imports[]
import io.micronaut.context.env.Environment;
import javax.inject.Inject;
// end::imports[]

// tag::class[]
public class RoundHandler extends MicronautRequestHandler<Float, Integer> { // <1>

    @Inject
    MathService mathService; // <2>

    @Inject
    Environment env;

    @Override
    public Integer execute(Float input) {
        return mathService.round(input); // <3>
    }
}
// end::class[]
