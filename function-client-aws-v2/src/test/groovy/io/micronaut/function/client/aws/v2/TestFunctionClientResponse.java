/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
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