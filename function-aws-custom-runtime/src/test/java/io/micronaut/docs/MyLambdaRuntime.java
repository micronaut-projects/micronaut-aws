package io.micronaut.docs;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.function.aws.runtime.AbstractMicronautLambdaRuntime;

import java.net.MalformedURLException;

public class MyLambdaRuntime
        extends AbstractMicronautLambdaRuntime<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent, Book, BookSaved> {
    public static void main(String[] args) {
        try {
            new MyLambdaRuntime().run(args);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
