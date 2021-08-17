package io.micronaut.docs.function.aws
//tag::clazz[]
import io.micronaut.context.env.Environment
import io.micronaut.function.aws.MicronautRequestStreamHandler

class Handler : MicronautRequestStreamHandler() {
    override fun resolveFunctionName(env: Environment): String {
        return "eventlogger"
    }
}
//end::clazz[]