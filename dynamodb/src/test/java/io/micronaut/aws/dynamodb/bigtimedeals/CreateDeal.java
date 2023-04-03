package io.micronaut.aws.dynamodb.bigtimedeals;

import io.micronaut.serde.annotation.Serdeable;

import java.math.BigDecimal;

@Serdeable
public class CreateDeal {

    private final String title;
    private final String link;
    private final BigDecimal price;
    private final String category;
    private final String brand;

    public CreateDeal(String title, String link, BigDecimal price, String category, String brand) {
        this.title = title;
        this.link = link;
        this.price = price;
        this.category = category;
        this.brand = brand;
    }

    public String getTitle() {
        return title;
    }

    public String getLink() {
        return link;
    }

    public BigDecimal getPrice() {
        return price;
    }

    public String getCategory() {
        return category;
    }

    public String getBrand() {
        return brand;
    }
}
