package io.micronaut.function.aws

import io.micronaut.context.ApplicationContextBuilder
import jakarta.inject.Inject

class SquareHandler extends MicronautRequestHandler<Integer, Integer> {
    SquareHandler() {
    }

    SquareHandler(ApplicationContextBuilder applicationContextBuilder) {
        super(applicationContextBuilder)
    }

    @Inject
    SquareService squareService

    @Override
    Integer execute(Integer input) {
        return squareService.square(input)
    }
}
