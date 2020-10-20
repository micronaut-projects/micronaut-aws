package io.micronaut.function.aws.logging

import com.amazonaws.services.lambda.runtime.ClientContext
import com.amazonaws.services.lambda.runtime.CognitoIdentity
import com.amazonaws.services.lambda.runtime.Context
import com.amazonaws.services.lambda.runtime.LambdaLogger
import io.micronaut.context.ApplicationContext
import io.micronaut.context.ApplicationContextBuilder
import io.micronaut.function.aws.MicronautRequestHandler
import io.micronaut.function.aws.SquareHandler
import spock.lang.AutoCleanup
import spock.lang.Shared
import spock.lang.Specification

abstract class ApplicationContextSpecification extends Specification {

    abstract MicronautRequestHandler instantiateHandler();

    @AutoCleanup
    @Shared
    MicronautRequestHandler handler = instantiateHandler()

    @Shared
    Context lambdaCtx = Mock(Context) {
        getLogger() >> Mock(LambdaLogger)
        getClientContext() >> Mock(ClientContext)
        getIdentity() >> Mock(CognitoIdentity)
    }

    @Shared
    @AutoCleanup
    ApplicationContext ctx

    ApplicationContext getApplicationContext() {
        if (ctx == null) {
            ctx = handler.buildApplicationContext(lambdaCtx)
        }
        ctx
    }
}
