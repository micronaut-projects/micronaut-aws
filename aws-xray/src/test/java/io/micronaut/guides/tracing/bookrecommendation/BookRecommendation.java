package io.micronaut.guides.tracing.bookrecommendation;

import io.micronaut.core.annotation.Introspected;

import java.util.Objects;

@Introspected
public class BookRecommendation {
    private String name;

    public BookRecommendation() {}

    public BookRecommendation(String name) {
        this.name = name;
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
        BookRecommendation that = (BookRecommendation) o;
        return Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {

        return Objects.hash(name);
    }
}
