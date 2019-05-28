package example;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.model.*;
import com.amazonaws.services.lambda.runtime.*;
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler;
import java.io.*;

public class StreamLambdaHandler implements RequestStreamHandler {
    private static MicronautLambdaContainerHandler handler; // <1>
    static {
        try {
            handler = MicronautLambdaContainerHandler.getAwsProxyHandler();
        } catch (ContainerInitializationException e) {
            // if we fail here. We re-throw the exception to force another cold start
            e.printStackTrace();
            throw new RuntimeException("Could not initialize Micronaut", e);
        }
    }

    @Override
    public void handleRequest(InputStream inputStream, OutputStream outputStream, Context context)
            throws IOException {
        handler.proxyStream(inputStream, outputStream, context); // <2>
    }
}