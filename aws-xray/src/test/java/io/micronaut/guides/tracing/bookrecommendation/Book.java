package io.micronaut.guides.tracing.bookrecommendation;

import java.util.Objects;

public class Book {

    private String isbn;
    private String name;

    public Book() {}

    public Book(String isbn, String name) {
        this.isbn = isbn;
        this.name =  name;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Book book = (Book) o;
        return Objects.equals(isbn, book.isbn) &&
                Objects.equals(name, book.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(isbn, name);
    }
}
