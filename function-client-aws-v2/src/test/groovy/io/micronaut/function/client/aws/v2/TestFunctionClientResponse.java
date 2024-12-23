package io.micronaut.function.client.aws.v2;

import io.micronaut.serde.annotation.Serdeable;

import java.util.List;

@Serdeable
public class TestFunctionClientResponse {
    private int aNumber;
    private String aString;
    private ComplexType aObject;
    private List<ComplexType> anArray;

    public TestFunctionClientResponse() {

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

    public List<ComplexType> getAnArray() {
        return anArray;
    }

    public void setAnArray(List<ComplexType> anArray) {
        this.anArray = anArray;
    }
}
