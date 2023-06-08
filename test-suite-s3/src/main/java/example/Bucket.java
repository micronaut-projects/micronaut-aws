package example;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Serdeable
public class Bucket {

    @NotNull
    @NotBlank
    private String name;

    public Bucket(@NotNull @NotBlank String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }
}
