package io.micronaut.guides.tracing.bookcatalogue;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Get;
import io.micronaut.security.annotation.Secured;
import io.micronaut.security.rules.SecurityRule;

import java.util.Arrays;
import java.util.List;

@Requires(property = "spec.name", value = "DistributingTracingGuideSpec.bookcatalogue")
@Controller("/books")
public class BooksController {

    @Secured(SecurityRule.IS_ANONYMOUS)
    @Get
    public List<Book> index() {
        Book buildingMicroservices = new Book("1491950358", "Building Microservices");
        Book releaseIt = new Book("1680502395", "Release It!");
        Book cidelivery = new Book("0321601912", "Continuous Delivery:");
        return Arrays.asList(buildingMicroservices, releaseIt, cidelivery);
    }
}
