package io.micronaut.guides.tracing.bookrecommendation;

import org.reactivestreams.Publisher;

public interface BookCatalogueOperations {
    Publisher<Book> findAll();
}
