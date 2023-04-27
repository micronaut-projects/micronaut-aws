package io.micronaut.aws.dynamodb.bigtimedeals;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Status;

@Requires(property = "spec.name", value = "BigTimeDealsTest")
@Controller("/deals")
public class DealsController {
    private final DealsRepository dealsRepository;

    public DealsController(DealsRepository dealsRepository) {
        this.dealsRepository = dealsRepository;
    }

    @Post
    @Status(HttpStatus.CREATED)
    void save(@Body CreateDeal deal) {

    }
}
