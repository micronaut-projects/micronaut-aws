package io.micronaut.function.aws

import io.micronaut.context.ApplicationContext
import jakarta.inject.Singleton

import java.util.concurrent.ConcurrentHashMap

@Singleton
class SingletonBean {

    static Map<String, Boolean> PACKAGE = new ConcurrentHashMap<>(3);

    SingletonBean(ApplicationContext ctx) {
        ctx.environment.packages.find()?.with {
            PACKAGE[it] = true
        }
    }
}
