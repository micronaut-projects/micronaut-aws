/*
 * Copyright 2017-2020 original authors
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.micronaut.function.aws.proxy.test;

import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.internal.testutils.MockLambdaContext;
import com.amazonaws.serverless.proxy.model.*;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.io.socket.SocketUtils;
import io.micronaut.function.aws.proxy.MicronautLambdaHandler;
import io.micronaut.http.HttpMethod;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.exceptions.HttpServerException;
import io.micronaut.http.server.exceptions.ServerStartupException;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.server.EmbeddedServer;
import org.apache.commons.io.IOUtils;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Enumeration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Implementation that spins up an HTTP server based on Jetty that proxies request to a Lambda.
 *
 * @author gkrocher
 * @since 2.1.0
 */
@Singleton
@Internal
public class AwsApiProxyTestServer implements EmbeddedServer {
    private final ApplicationContext applicationContext;
    private final boolean randomPort;
    private int port;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Server server;

    public AwsApiProxyTestServer(
            ApplicationContext applicationContext,
            HttpServerConfiguration httpServerConfiguration) {
        this.applicationContext = applicationContext;
        Optional<Integer> port = httpServerConfiguration.getPort();
        if (port.isPresent()) {
            this.port = port.get();
            if (this.port == -1) {
                this.port = SocketUtils.findAvailableTcpPort();
                this.randomPort = true;
            } else {
                this.randomPort = false;
            }
        } else {
            if (applicationContext.getEnvironment().getActiveNames().contains(Environment.TEST)) {
                this.randomPort = true;
                this.port = SocketUtils.findAvailableTcpPort();
            } else {
                this.randomPort = false;
                this.port = 8080;
            }
        }
    }

    @Override
    public EmbeddedServer start() {
        if (running.compareAndSet(false, true)) {
            int retryCount = 0;
            while (retryCount <= 3) {
                try {
                    this.server = new Server(port);
                    this.server.setHandler(new AwsProxyHandler());
                    this.server.start();
                    break;
                } catch (BindException e) {
                    if (randomPort) {
                        this.port = SocketUtils.findAvailableTcpPort();
                        retryCount++;
                    } else {
                        throw new ServerStartupException(e.getMessage(), e);
                    }
                } catch (Exception e) {
                    throw new ServerStartupException(e.getMessage(), e);
                }
            }
            if (server == null) {
                throw new HttpServerException("No available ports");
            }
        }
        return this;
    }

    @Override
    public EmbeddedServer stop() {
        if (running.compareAndSet(true, false)) {
            try {
                server.stop();
            } catch (Exception e) {
                // ignore / unrecoverable
            }
        }
        return this;
    }

    @Override
    public int getPort() {
        return port;
    }

    @Override
    public String getHost() {
        return "localhost";
    }

    @Override
    public String getScheme() {
        return "http";
    }

    @Override
    public URL getURL() {
        String spec = getScheme() + "://" + getHost() + ":" + getPort();
        try {
            return new URL(spec);
        } catch (MalformedURLException e) {
            throw new HttpServerException("Invalid server URL " + spec);
        }
    }

    @Override
    public URI getURI() {
        try {
            return getURL().toURI();
        } catch (URISyntaxException e) {
            throw new HttpServerException("Invalid server URL " + getURL());
        }
    }

    @Override
    public ApplicationContext getApplicationContext() {
        return applicationContext;
    }

    @Override
    public ApplicationConfiguration getApplicationConfiguration() {
        return applicationContext.getBean(ApplicationConfiguration.class);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private static class AwsProxyHandler extends AbstractHandler {

        private final MicronautLambdaHandler lambdaHandler;

        public AwsProxyHandler() throws ContainerInitializationException {
            lambdaHandler = new MicronautLambdaHandler(ApplicationContext.builder());
        }

        @Override
        public void destroy() {
            super.destroy();
            this.lambdaHandler.close();
        }

        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            AwsProxyRequest awsProxyRequest = new AwsProxyRequest();
            AwsProxyRequestContext requestContext = new AwsProxyRequestContext();
            requestContext.setIdentity(new ApiGatewayRequestIdentity());
            requestContext.setHttpMethod(request.getMethod());
            requestContext.setRequestTimeEpoch(Instant.now().toEpochMilli());
            awsProxyRequest.setRequestContext(requestContext);
            awsProxyRequest.setHttpMethod(request.getMethod());
            awsProxyRequest.setPath(request.getRequestURI());
            Headers requestHeaders = new Headers();
            awsProxyRequest.setMultiValueHeaders(requestHeaders);
            Map<String, String[]> parameterMap = request.getParameterMap();
            MultiValuedTreeMap<String, String> params = new MultiValuedTreeMap<>();
            awsProxyRequest.setMultiValueQueryStringParameters(params);
            parameterMap.forEach((s, strings) -> params.addAll(s, s));
            Enumeration<String> headerNames = request.getHeaderNames();
            while (headerNames.hasMoreElements()) {
                String s = headerNames.nextElement();
                Enumeration<String> headers = request.getHeaders(s);
                while (headers.hasMoreElements()) {
                    String v = headers.nextElement();
                    requestHeaders.add(s, v);
                }
            }
            HttpMethod httpMethod = HttpMethod.parse(request.getMethod());
            if (HttpMethod.permitsRequestBody(httpMethod)) {
                try (InputStream requestBody = request.getInputStream()) {
                    byte[] data = IOUtils.toByteArray(requestBody);
                    awsProxyRequest.setIsBase64Encoded(true);
                    awsProxyRequest.setBody(Base64.getEncoder().encodeToString(data));
                } catch (IOException e) {
                    // ignore
                }
            }

            AwsProxyResponse awsProxyResponse = lambdaHandler
                    .handleRequest(awsProxyRequest, new MockLambdaContext());
            Headers responseHeaders = awsProxyResponse.getMultiValueHeaders();

            responseHeaders.forEach((key, strings) -> {
                for (String string : strings) {
                    response.addHeader(key, string);
                }
            });
            response.setStatus(awsProxyResponse.getStatusCode());
            if (httpMethod != HttpMethod.HEAD && httpMethod != HttpMethod.OPTIONS) {
                byte[] bodyAsBytes = new byte[0];
                if (awsProxyResponse.isBase64Encoded()) {
                    String body = awsProxyResponse.getBody();
                    if (body != null) {
                        bodyAsBytes = Base64.getDecoder().decode(body);
                    }
                } else {
                    String body = awsProxyResponse.getBody();
                    if (body != null) {
                        bodyAsBytes = body.getBytes(StandardCharsets.UTF_8);
                    }
                }

                response.setContentLength(bodyAsBytes.length);
                if (bodyAsBytes.length > 0) {
                    try (OutputStream responseBody = response.getOutputStream()) {
                        responseBody.write(bodyAsBytes);
                        responseBody.flush();
                    }
                }
            }

        }
    }
}
