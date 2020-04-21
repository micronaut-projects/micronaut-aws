package io.micronaut.docs;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.function.aws.MicronautRequestHandler;

@Introspected
class CustomMicronautRequestHandler extends MicronautRequestHandler<Book, BookSaved> {

    @Override
    public BookSaved execute(Book input) {
        BookSaved bookSaved = new BookSaved();
        bookSaved.setName(input.getName());
        bookSaved.setIsbn("XXX");
        return bookSaved;
    }
}
