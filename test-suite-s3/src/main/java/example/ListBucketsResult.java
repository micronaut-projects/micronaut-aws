package example;

import io.micronaut.serde.annotation.Serdeable;
import jakarta.annotation.Nullable;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.List;

@Serdeable
public class ListBucketsResult extends Result{

    @Nullable
    private List<String> buckets;

    public ListBucketsResult(String requestId, @NotNull @NotBlank String status, @Nullable List<String> buckets) {
        super(requestId, status, null);
        this.buckets = buckets;
    }

    public List<String> getBuckets() {
        return buckets;
    }
}
