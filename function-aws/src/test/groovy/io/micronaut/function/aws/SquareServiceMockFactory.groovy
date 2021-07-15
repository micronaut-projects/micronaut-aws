package io.micronaut.function.aws

import io.micronaut.context.annotation.Bean
import io.micronaut.context.annotation.Factory
import spock.mock.DetachedMockFactory

import jakarta.inject.Singleton

@Factory
class SquareServiceMockFactory {

    @Bean
    @Singleton
    SquareService mockEchoService() {
        return new DetachedMockFactory().Mock(SquareService)
    }
}
