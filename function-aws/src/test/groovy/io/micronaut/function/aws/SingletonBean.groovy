package io.micronaut.function.aws

import io.micronaut.context.ApplicationContext
import jakarta.inject.Singleton

@Singleton
class SingletonBean {

    static String PACKAGE = null;

    SingletonBean(ApplicationContext ctx) {
        PACKAGE = ctx.environment.packages.find()
    }
}
