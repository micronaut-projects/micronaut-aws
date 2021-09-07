package io.micronaut.function.aws

import jakarta.inject.Inject

class SquareHandler extends MicronautRequestHandler<Integer, Integer> {

    @Inject
    SquareService squareService

    @Override
    Integer execute(Integer input) {
        return squareService.square(input)
    }
}
