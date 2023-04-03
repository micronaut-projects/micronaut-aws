package io.micronaut.aws.dynamodb.bigtimedeals;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.annotation.Body;
import io.micronaut.http.annotation.Controller;
import io.micronaut.http.annotation.Post;
import io.micronaut.http.annotation.Status;

@Requires(property = "spec.name", value = "BigTimeDealsTest")
@Controller("/brands")
public class BrandsController {

    private final BrandsRepository brandsRepository;

    public BrandsController(BrandsRepository brandsRepository) {
        this.brandsRepository = brandsRepository;
    }

    @Status(HttpStatus.CREATED)
    @Post
    void save(@Body CreateBrand brand) {
        brandsRepository.save(brand);
    }
}
