package io.micronaut.function.client.aws.v2;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class ComplexType {
    private int aNumber;
    private String aString;

    public ComplexType() {
    }

    public ComplexType(int aNumber, String aString) {
        this.aNumber = aNumber;
        this.aString = aString;
    }

    public int getaNumber() {
        return aNumber;
    }

    public void setaNumber(int aNumber) {
        this.aNumber = aNumber;
    }

    public String getaString() {
        return aString;
    }

    public void setaString(String aString) {
        this.aString = aString;
    }
}
