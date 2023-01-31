package io.micronaut.function.aws.runtime;

import java.net.MalformedURLException;

import com.amazonaws.services.lambda.runtime.RequestHandler;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.function.aws.proxy.MicronautApiGatewayV1ContainerHandler;

public class MicronautApiGatewayPayloadV1Runtime
    extends AbstractMicronautPayloadRuntime<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {

    @Override
    protected RequestHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> createRequestHandler(final String... args) {
        return new MicronautApiGatewayV1ContainerHandler(createApplicationContextBuilderWithArgs(args));
    }

    /**
     *
     * @param args Command Line args
     */
    public static void main(String[] args) {
        try {
            new MicronautApiGatewayPayloadV1Runtime().run(args);
        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }

}
