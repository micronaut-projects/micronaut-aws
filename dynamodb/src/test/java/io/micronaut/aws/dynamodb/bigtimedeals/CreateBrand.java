package io.micronaut.aws.dynamodb.bigtimedeals;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class CreateBrand {
    private final String name;
    private final String logoUrl;


    public CreateBrand(String name, String logoUrl) {
        this.name = name;
        this.logoUrl = logoUrl;
    }

    public String getName() {
        return name;
    }

    public String getLogoUrl() {
        return logoUrl;
    }
}
