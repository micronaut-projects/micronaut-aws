package io.micronaut.function.aws.runtime.micronautrequeststreamhandler

import io.micronaut.context.env.Environment
import io.micronaut.core.annotation.Introspected
import io.micronaut.function.aws.MicronautRequestStreamHandler

@Introspected
class FunctionRequestHandler extends MicronautRequestStreamHandler {

    @Override
    protected String resolveFunctionName(Environment env) {
        "requestfunction"
    }
}
