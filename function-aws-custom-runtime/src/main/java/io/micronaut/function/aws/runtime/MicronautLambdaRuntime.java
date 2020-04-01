/*
 * Copyright 2017-2019 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.function.aws.runtime;

import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.core.cli.CommandLine;
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.aws.MicronautLambdaContext;
import io.micronaut.function.aws.proxy.MicronautLambdaContainerHandler;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.client.BlockingHttpClient;
import io.micronaut.http.client.DefaultHttpClientConfiguration;
import io.micronaut.http.client.RxHttpClient;
import io.micronaut.runtime.context.env.CommandLinePropertySource;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.function.Predicate;

/**
 * Class that can be used as a entry point for a custom Lambda runtime.
 *
 * @author graemerocher
 * @since 1.1
 */
public class MicronautLambdaRuntime implements AwsLambdaRuntimeApi {

    /**
     * Main entry point.
     *
     * @param args The arguments
     * @throws Exception If an error occurs initializing the custom runtime
     */
    public static void main(String... args) throws Exception {
        final URL runtimeApiURL = lookupRuntimeApiEndpoint();
        final Predicate<URL> loopUntil = (url) -> true;

        CommandLine commandLine = CommandLine.parse(args);
        final ApplicationContextBuilder applicationContextBuilder = ApplicationContext.build()
                .environments(MicronautLambdaContext.ENVIRONMENT_LAMBDA)
                .propertySources(new CommandLinePropertySource(commandLine));

        new MicronautLambdaRuntime()
                .startRuntimeApiEventLoop(runtimeApiURL, applicationContextBuilder, loopUntil);
    }

    /**
     * Starts the runtime API event loop.
     *
     * @param applicationContextBuilder The context builder
     * @throws MalformedURLException if the lambda endpoint URL is malformed
     */
    public void startRuntimeApiEventLoop(
            @Nonnull ApplicationContextBuilder applicationContextBuilder) throws MalformedURLException {
        startRuntimeApiEventLoop(lookupRuntimeApiEndpoint(), applicationContextBuilder, (url) -> true);
    }

    /**
     * Starts the runtime API event loop.
     *
     * @param runtimeApiURL             The runtime API URL.
     * @param applicationContextBuilder The context builder
     */
    public void startRuntimeApiEventLoop(
            @Nonnull URL runtimeApiURL,
            @Nonnull ApplicationContextBuilder applicationContextBuilder) {
        startRuntimeApiEventLoop(runtimeApiURL, applicationContextBuilder, (url) -> true);
    }

    /**
     * Starts the runtime API event loop.
     *
     * @param runtimeApiURL             The runtime API URL.
     * @param applicationContextBuilder The context builder
     * @param loopUntil                 A predicate that allows controlling when the event loop should exit
     */
    public void startRuntimeApiEventLoop(
            @Nonnull URL runtimeApiURL,
            @Nonnull ApplicationContextBuilder applicationContextBuilder,
            @Nonnull Predicate<URL> loopUntil) {
        try {
            final MicronautLambdaContainerHandler handler = new MicronautLambdaContainerHandler(applicationContextBuilder);
            final DefaultHttpClientConfiguration config = new DefaultHttpClientConfiguration();
            config.setReadIdleTimeout(null);
            config.setReadTimeout(null);
            final RxHttpClient endpointClient = handler.getApplicationContext().createBean(
                    RxHttpClient.class,
                    runtimeApiURL,
                    config);
            try {
                while (loopUntil.test(runtimeApiURL)) {
                    final BlockingHttpClient blockingHttpClient = endpointClient.toBlocking();
                    final HttpResponse<AwsProxyRequest> response = blockingHttpClient.exchange(AwsLambdaRuntimeApi.NEXT_INVOCATION_URI, AwsProxyRequest.class);

                    final AwsProxyRequest awsProxyRequest = response.body();
                    if (awsProxyRequest != null) {
                        final HttpHeaders headers = response.getHeaders();
                        final String requestId = headers.get(LambdaRuntimeInvocationResponseHeaders.LAMBDA_RUNTIME_AWS_REQUEST_ID);
                        try {
                            if (StringUtils.isNotEmpty(requestId)) {
                                final AwsProxyResponse awsProxyResponse = handler.proxy(awsProxyRequest, new RuntimeContext(headers));
                                endpointClient.exchange(invocationResponseRequest(requestId, awsProxyResponse)).blockingSubscribe();
                            }
                        } catch (Throwable e) {
                            final StringWriter sw = new StringWriter();
                            e.printStackTrace(new PrintWriter(sw));
                            System.out.println("Invocation with requestId [" + requestId + "] failed: " + e.getMessage() + sw.toString());
                            try {
                                blockingHttpClient.exchange(invocationErrorRequest(requestId, e.getMessage(), null, null));
                            } catch (Throwable e2) {
                                // swallow, nothing we can do...
                            }
                        }
                    }
                }
            } finally {
                handler.close();
                if (endpointClient != null) {
                    endpointClient.close();
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println("Request loop failed with: " + e.getMessage());
            try (RxHttpClient endpointClient = RxHttpClient.create(runtimeApiURL)) {

                endpointClient.toBlocking().exchange(initializationErrorRequest(e.getMessage(), null, null));
            } catch (Throwable e2) {
                // swallow, nothing we can do...
            }
        }
    }

    private static URL lookupRuntimeApiEndpoint() throws MalformedURLException {
        final String runtimeApiEndpoint = System.getenv("AWS_LAMBDA_RUNTIME_API");
        if (StringUtils.isEmpty(runtimeApiEndpoint)) {
            throw new IllegalStateException("Missing AWS_LAMBDA_RUNTIME_API environment variable. Custom runtime can only be run within AWS Lambda environment.");
        }
        return new URL("http://" + runtimeApiEndpoint);
    }
}
