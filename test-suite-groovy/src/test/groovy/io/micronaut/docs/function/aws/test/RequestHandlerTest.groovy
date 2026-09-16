package io.micronaut.docs.function.aws.test

//tag::clazz[]
import io.micronaut.context.ApplicationContext
import io.micronaut.function.aws.test.annotation.MicronautLambdaTest
import jakarta.inject.Inject
import org.junit.jupiter.api.Test

@MicronautLambdaTest
class RequestHandlerTest {
    @Inject
    ApplicationContext context

    @Test
    void testHandler() {
        SampleRequestHandler sampleRequestHandler = new SampleRequestHandler(context)
        assert sampleRequestHandler.execute("world") == "Hello world"
    }
}
//end::clazz[]
