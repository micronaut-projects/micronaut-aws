package io.micronaut.docs.function.aws.test;

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
