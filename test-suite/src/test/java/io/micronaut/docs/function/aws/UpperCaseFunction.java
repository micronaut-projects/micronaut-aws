package io.micronaut.docs.function.aws;

import io.micronaut.function.FunctionBean;

import java.util.function.Function;

@FunctionBean("uppercase")
public class UpperCaseFunction implements Function<String, String> {
    @Override
    public String apply(String s) {
        return s.toUpperCase();
    }
}
