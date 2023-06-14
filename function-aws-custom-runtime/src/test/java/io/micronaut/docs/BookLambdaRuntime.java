package io.micronaut.docs;

import io.micronaut.aws.lambda.events.APIGatewayProxyRequestEvent;
import io.micronaut.aws.lambda.events.APIGatewayProxyResponseEvent;
import io.micronaut.function.aws.runtime.AbstractMicronautLambdaRuntime;

import java.net.MalformedURLException;

public class BookLambdaRuntime
        extends AbstractMicronautLambdaRuntime<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent, Book, BookSaved> {
    public static void main(String[] args) {
        try {
            new BookLambdaRuntime().run(args);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected Object createHandler(String... args) {
        return new BookRequestHandler();
    }
}
