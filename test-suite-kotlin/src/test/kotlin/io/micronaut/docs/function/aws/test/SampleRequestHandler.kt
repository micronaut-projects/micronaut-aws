package io.micronaut.docs.function.aws.test

//tag::clazz[]
import io.micronaut.context.ApplicationContext
import io.micronaut.function.aws.MicronautRequestHandler

class SampleRequestHandler : MicronautRequestHandler<String, String> {
    // Used in AWS
    constructor() : super()

    // Used in tests
    constructor(applicationContext: ApplicationContext) : super(applicationContext)

    override fun execute(input: String): String {
        return "Hello $input"
    }
}
//end::clazz[]
