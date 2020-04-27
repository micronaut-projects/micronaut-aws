package io.micronaut.function.aws.runtime;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.services.lambda.runtime.RequestHandler;
import io.micronaut.context.exceptions.ConfigurationException;
import io.micronaut.function.aws.proxy.MicronautLambdaHandler;

import java.net.MalformedURLException;

/**
 * Main entry for AWS API proxy with Micronaut.
 *
 * @author sdelamo
 * @since 2.0.0
 */
public class MicronautLambdaRuntime extends AbstractMicronautLambdaRuntime<AwsProxyRequest, AwsProxyResponse, AwsProxyRequest, AwsProxyResponse> {

    @Override
    protected RequestHandler<AwsProxyRequest, AwsProxyResponse> createRequestHandler(String... args) {
        try {
            return new MicronautLambdaHandler(createApplicationContextBuilderWithArgs(args));
        } catch (ContainerInitializationException e) {
            throw new ConfigurationException("Exception thrown instantiating MicronautLambdaRuntimeHandler");
        }
    }

    /**
     *
     * @param args Command Line args
     */
    public static void main(String[] args) {
        try {
            new MicronautLambdaRuntime().run(args);

        } catch (MalformedURLException e) {
            e.printStackTrace();
        }
    }
}
