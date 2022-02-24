package io.micronaut.guides.tracing.bookcatalogue;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;

import javax.validation.constraints.NotBlank;

@Introspected
public class Book {

    @NonNull
    @NotBlank
    private String isbn;
    @NonNull
    @NotBlank

    private String name;

    public Book() {}

    public Book(@NonNull @NotBlank String isbn, @NonNull @NotBlank String name) {
        this.isbn = isbn;
        this.name = name;
    }

    @NonNull
    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(@NonNull String isbn) {
        this.isbn = isbn;
    }

    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Book book = (Book) o;

        if (!isbn.equals(book.isbn)) return false;
        return name.equals(book.name);
    }

    @Override
    public int hashCode() {
        int result = isbn.hashCode();
        result = 31 * result + name.hashCode();
        return result;
    }
}
