package io.micronaut.function.client.aws.v2;

import io.micronaut.serde.annotation.Serdeable;

@Serdeable
public class TestFunctionClientRequest {
    private int aNumber;
    private String aString;
    private ComplexType aObject;

    public TestFunctionClientRequest() {
    }

    public TestFunctionClientRequest(int aNumber, String aString, ComplexType aObject) {
        this.aNumber = aNumber;
        this.aString = aString;
        this.aObject = aObject;
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

    public ComplexType getaObject() {
        return aObject;
    }

    public void setaObject(ComplexType aObject) {
        this.aObject = aObject;
    }
}
