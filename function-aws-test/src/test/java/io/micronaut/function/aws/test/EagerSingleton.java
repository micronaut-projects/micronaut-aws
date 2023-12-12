package io.micronaut.function.aws.test;

import jakarta.inject.Singleton;

@Singleton
public class EagerSingleton {

    public String hello(String name) {
        return "hello %s".formatted(name);
    }
}
