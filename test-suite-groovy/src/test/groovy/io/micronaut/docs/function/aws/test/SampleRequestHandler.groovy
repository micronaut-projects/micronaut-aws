package io.micronaut.docs.function.aws.test

//tag::clazz[]
import groovy.transform.CompileStatic
import io.micronaut.context.ApplicationContext
import io.micronaut.function.aws.MicronautRequestHandler

@CompileStatic
class SampleRequestHandler extends MicronautRequestHandler<String, String> {
    // Used in AWS
    SampleRequestHandler() {
    }

    // Used in tests
    SampleRequestHandler(ApplicationContext applicationContext) {
        super(applicationContext)
    }

    @Override
    String execute(String input) {
        "Hello " + input
    }
}
//end::clazz[]
