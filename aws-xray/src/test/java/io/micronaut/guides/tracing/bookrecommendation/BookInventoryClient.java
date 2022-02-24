package io.micronaut.guides.tracing.bookrecommendation;

import io.micronaut.context.annotation.Requires;
import io.micronaut.core.async.annotation.SingleResult;
import io.micronaut.http.MediaType;
import io.micronaut.http.annotation.Consumes;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.retry.annotation.Recoverable;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Mono;

import javax.validation.constraints.NotBlank;

@Requires(property = "spec.name", value = "DistributingTracingGuideSpec.bookrecommendation")
@Client(id = "bookinventory")
@Recoverable(api = BookInventoryOperations.class)
public interface BookInventoryClient extends BookInventoryOperations {

    @Consumes(MediaType.TEXT_PLAIN)
    @Get("/books/stock/{isbn}")
    @SingleResult
    Publisher<Boolean> stock(@NotBlank String isbn);
}
