package io.micronaut.docs.function.aws

//tag::clazz[]
import io.micronaut.context.env.Environment
import io.micronaut.core.annotation.Introspected
import io.micronaut.function.aws.MicronautRequestStreamHandler

@Introspected
class Handler extends MicronautRequestStreamHandler {
    @Override
    protected String resolveFunctionName(Environment env) {
        'eventlogger'
    }
}
//end::clazz[]

