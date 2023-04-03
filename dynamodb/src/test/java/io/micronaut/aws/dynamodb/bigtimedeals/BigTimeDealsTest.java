package io.micronaut.aws.dynamodb.bigtimedeals;

import io.micronaut.context.annotation.Property;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.HttpClient;
import io.micronaut.http.client.annotation.Client;
import io.micronaut.test.extensions.junit5.annotation.MicronautTest;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.Assert.assertEquals;

@Property(name = "dynamodb.table-name", value = BigTimeDealsTest.TABLE_NAME)
@Property(name = "spec.name", value = "BigTimeDealsTest")
@MicronautTest
class BigTimeDealsTest {

    public static final String TABLE_NAME = "bigtimedeals";
    @Inject
    @Client("/")
    HttpClient httpClient;

    @Test
    void bigTimeDealsTest() {
        BlockingHttpClient client = httpClient.toBlocking();

        String brand = "Nike";

        HttpRequest<?> createBrandRequest = HttpRequest.POST("/brands", new CreateBrand(brand, "https://static.nike.com/logo.png" ));

//        HttpResponse<?> createBrandResponse = client.exchange(createBrandRequest);
//        assertEquals(HttpStatus.CREATED, createBrandResponse.getStatus());
//
//        HttpRequest<?> createDealRequest = HttpRequest.POST("/deals", new CreateDeal("8 day cruise!", "https://www.princess.com/...", new BigDecimal("1599"), "Travel",  brand));
//        assertEquals(HttpStatus.CREATED, createBrandResponse.getStatus());

    }



}
