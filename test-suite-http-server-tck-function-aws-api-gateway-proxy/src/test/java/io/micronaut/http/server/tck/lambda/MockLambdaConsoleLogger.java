package io.micronaut.http.server.tck.lambda;

import com.amazonaws.services.lambda.runtime.LambdaLogger;

import java.nio.charset.Charset;

public class MockLambdaConsoleLogger implements LambdaLogger {

    @Override
    public void log(String s) {
        System.out.println(s);
    }


    @Override
    public void log(byte[] bytes) {
        System.out.println(new String(bytes, Charset.defaultCharset()));
    }
}
