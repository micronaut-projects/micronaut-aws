package io.micronaut.docs.function.aws;

import io.micronaut.core.util.StringUtils;
import io.micronaut.function.FunctionBean;
import java.util.function.Function;

@FunctionBean("capitalize")
public class CapitalizeFunction implements Function<String, String> {
    @Override
    public String apply(String s) {
        return StringUtils.capitalize(s.toLowerCase());
    }
}
