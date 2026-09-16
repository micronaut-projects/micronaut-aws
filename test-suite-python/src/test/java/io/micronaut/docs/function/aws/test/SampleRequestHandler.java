package io.micronaut.docs.function.aws.test;

// An AWS Lambda handler must extend the Java class MicronautRequestHandler, which a Python class cannot do, so the
// handler tested by the Python RequestHandlerTest is this Java copy of the test-suite SampleRequestHandler.

//tag::clazz[]
import io.micronaut.context.ApplicationContext;
import io.micronaut.function.aws.MicronautRequestHandler;

public class SampleRequestHandler extends MicronautRequestHandler<String, String> {
    // Used in AWS
    public SampleRequestHandler() {
    }

    // Used in tests
    public SampleRequestHandler(ApplicationContext applicationContext) {
        super(applicationContext);
    }

    @Override
    public String execute(String input) {
        return "Hello " + input;
    }
}
//end::clazz[]
