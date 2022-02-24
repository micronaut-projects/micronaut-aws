package io.micronaut.guides.tracing.bookrecommendation;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.annotation.Get;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.retry.annotation.Recoverable;
import org.reactivestreams.Publisher;

@Requires(property = "spec.name", value = "DistributingTracingGuideSpec.bookrecommendation")
@Client(id ="bookcatalogue")
@Recoverable(api = BookCatalogueOperations.class)
public interface BookCatalogueClient extends BookCatalogueOperations {

    @Get("/books")
    Publisher<Book> findAll();
}