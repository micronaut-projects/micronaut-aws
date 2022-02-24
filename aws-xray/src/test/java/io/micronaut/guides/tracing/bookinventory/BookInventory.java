package io.micronaut.guides.tracing.bookinventory;

import io.micronaut.core.annotation.Introspected;
import io.micronaut.core.annotation.NonNull;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import java.util.Objects;

@Introspected
public class BookInventory {

    @NonNull
    @NotBlank
    private String isbn;

    @NonNull
    @NotNull
    private Integer stock;

    public BookInventory() {}

    public BookInventory(@NonNull @NotBlank String isbn, @NonNull @NotNull Integer stock) {
        this.isbn = isbn;
        this.stock = stock;
    }

    @NonNull
    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(@NonNull String isbn) {
        this.isbn = isbn;
    }

    @NonNull
    public Integer getStock() {
        return stock;
    }

    public void setStock(@NonNull Integer stock) {
        this.stock = stock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BookInventory that = (BookInventory) o;

        if (!isbn.equals(that.isbn)) return false;
        return stock.equals(that.stock);
    }

    @Override
    public int hashCode() {
        int result = isbn.hashCode();
        result = 31 * result + stock.hashCode();
        return result;
    }
}
