/*
 * Copyright 2017-2021 original authors
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
package com.example;

import io.micronaut.context.annotation.EachProperty;
import io.micronaut.core.annotation.Introspected;

import java.util.Collections;
import java.util.List;

@EachProperty("owners")
@Introspected
public class OwnerConfiguration {
    private String name;
    private int age;
    private List<String> pets = Collections.emptyList();

    public void setPets(List<String> pets) {
        this.pets = pets;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setAge(int age) {
        this.age = age;
    }

    Owner create() {
        return new Owner(name, age, pets);
    }
}