package io.micronaut.aws.dynamodb.ecommerce;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class Address {
    private final String streetAddress;
    private final String postalCode;
    private final String country;

    public Address(String streetAddress, String postalCode, String country) {
        this.streetAddress = streetAddress;
        this.postalCode = postalCode;
        this.country = country;
    }

    public String getStreetAddress() {
        return streetAddress;
    }

    public String getPostalCode() {
        return postalCode;
    }

    public String getCountry() {
        return country;
    }
}
