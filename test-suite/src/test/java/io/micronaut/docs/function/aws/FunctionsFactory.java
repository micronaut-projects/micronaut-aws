package io.micronaut.docs.function.aws;

import io.micronaut.context.annotation.Factory;
import io.micronaut.function.FunctionBean;
import java.util.function.Function;

@Factory
public class FunctionsFactory {
    @FunctionBean("reverse")
    public Function<String, String> helloWorld() {
        return FunctionsFactory::reverse;
    }

    private static String reverse(String input) {
        byte[] strAsByteArray = input.getBytes();
        byte[] result = new byte[strAsByteArray.length];
        for (int i = 0; i < strAsByteArray.length; i++) {
            result[i] = strAsByteArray[strAsByteArray.length - i - 1];
        }
        return new String(result);
    }
}