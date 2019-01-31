package io.micronaut.function.aws

import javax.inject.Singleton

@Singleton
class MathService {
    Integer round(Float input) {
        return Math.round(input)
    }
}
