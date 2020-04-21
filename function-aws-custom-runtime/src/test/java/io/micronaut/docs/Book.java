package io.micronaut.docs;

//tag::imports[]
import edu.umd.cs.findbugs.annotations.NonNull;
import io.micronaut.core.annotation.Introspected;
import javax.validation.constraints.NotBlank;
//end::imports[]

//tag::clazz[]
@Introspected // <1>
public class Book {

    @NonNull
    @NotBlank
    private String name;

    public Book() {
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
}
