package io.micronaut.docs.function.aws.test;

//tag::clazz[]
import io.micronaut.context.ApplicationContext;
import io.micronaut.function.aws.test.annotation.MicronautLambdaTest;
import org.junit.jupiter.api.Test;

import jakarta.inject.Inject;

import static org.junit.jupiter.api.Assertions.assertEquals;

@MicronautLambdaTest
public class RequestHandlerTest {
    @Inject
    private ApplicationContext context;

    @Test
    void testHandler() {
        SampleRequestHandler sampleRequestHandler = new SampleRequestHandler(context);
        assertEquals("Hello world", sampleRequestHandler.execute("world"));
    }
}
//end::clazz[]
