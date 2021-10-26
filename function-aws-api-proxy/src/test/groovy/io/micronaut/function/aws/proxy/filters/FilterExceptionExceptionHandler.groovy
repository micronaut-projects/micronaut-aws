package io.micronaut.function.aws.proxy.filters

import io.micronaut.http.HttpRequest
import io.micronaut.http.HttpResponse
import io.micronaut.http.server.exceptions.ExceptionHandler
import jakarta.inject.Singleton

@Singleton
class FilterExceptionExceptionHandler implements ExceptionHandler<FilterExceptionException, HttpResponse<?>> {

    @Override
    HttpResponse<?> handle(HttpRequest request, FilterExceptionException exception) {
        throw new RuntimeException('from exception handler')
    }
}
