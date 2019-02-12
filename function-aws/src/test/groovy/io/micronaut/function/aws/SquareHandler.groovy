package io.micronaut.function.aws

import javax.inject.Inject

class SquareHandler extends MicronautRequestHandler<Integer, Integer> {

    @Inject
    SquareService squareService

    @Override
    Integer execute(Integer input) {
        return squareService.square(input)
    }
}
