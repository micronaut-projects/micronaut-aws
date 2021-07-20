package io.micronaut.function.aws

import io.micronaut.aop.MethodInterceptor
import io.micronaut.aop.MethodInvocationContext
import io.micronaut.context.annotation.Replaces
import io.micronaut.runtime.context.scope.refresh.RefreshInterceptor
import io.micronaut.runtime.context.scope.refresh.RefreshScope
import spock.lang.Issue
import jakarta.inject.Singleton
//TODO delete once Micronaut 2.0.0.M3 is released
@Issue("https://github.com/micronaut-projects/micronaut-core/issues/3072")
@Replaces(RefreshInterceptor)
@Singleton
class RefreshInterceptorWorkAround  implements MethodInterceptor {

    @Override
    Object intercept(MethodInvocationContext context) {
        return context.proceed()
    }
}
