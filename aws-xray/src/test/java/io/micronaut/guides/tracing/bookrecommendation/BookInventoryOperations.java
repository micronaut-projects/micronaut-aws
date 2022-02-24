package io.micronaut.guides.tracing.bookrecommendation;

import io.micronaut.core.async.annotation.SingleResult;
import org.reactivestreams.Publisher;
import javax.validation.constraints.NotBlank;

public interface BookInventoryOperations {
    @SingleResult
    Publisher<Boolean> stock(@NotBlank String isbn);
}
