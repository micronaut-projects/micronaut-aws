package example;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

@Serdeable
public class Result {

    private final String requestId;

    @NotNull
    @NotBlank
    private final String status;

    @Nullable
    private final String message;

    public Result(String requestId, @NotNull @NotBlank String status, @Nullable String message) {
        this.requestId = requestId;
        this.status = status;
        this.message = message;
    }

    public String getMessage() {
        return message;
    }

    public String getStatus() {
        return status;
    }

    public String getRequestId() {
        return requestId;
    }
}
