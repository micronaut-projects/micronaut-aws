package io.micronaut.docs;

//tag::imports[]
import io.micronaut.core.annotation.NonNull;
import io.micronaut.core.annotation.Introspected;
//end::imports[]
//tag::clazz[]
@Introspected // <1>
public class BookSaved {

    @NonNull
    private String name;

    @NonNull
    private String isbn;

    public BookSaved() {

    }

    // Getters & Setters
//end::clazz[]
    @NonNull
    public String getName() {
        return name;
    }

    public void setName(@NonNull String name) {
        this.name = name;
    }

    @NonNull
    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(@NonNull String isbn) {
        this.isbn = isbn;
    }
}
