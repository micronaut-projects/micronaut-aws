package io.micronaut.function.aws.runtime

import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.context.annotation.Replaces
import io.micronaut.runtime.context.scope.refresh.RefreshInterceptor
import spock.lang.Issue

import javax.inject.Singleton

//TODO delete once Micronaut 2.0.0.M3 is released
@Issue("https://github.com/micronaut-projects/micronaut-core/issues/3072")
@Replaces(RefreshInterceptor)
@Singleton
class RefreshInterceptorWorkAround implements MethodInterceptor {

    @Override
    Object intercept(MethodInvocationContext context) {
        return context.proceed()
    }
}
