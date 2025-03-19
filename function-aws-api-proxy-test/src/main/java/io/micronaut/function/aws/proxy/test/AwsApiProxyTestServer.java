/*
 * Copyright 2017-2023 original authors
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

import java.io.IOException;
import java.io.OutputStream;
import java.net.*;
import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.core.util.StringUtils;
import io.micronaut.function.aws.proxy.payload2.APIGatewayV2HTTPEventFunction;
import io.micronaut.http.HttpHeaders;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.exceptions.HttpServerException;
import io.micronaut.http.server.exceptions.ServerStartupException;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.server.EmbeddedServer;
import jakarta.inject.Singleton;
import com.sun.net.httpserver.HttpServer;

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
    private final APIGatewayV2HTTPEventFunction handler;
    private final ServerPort serverPort;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private HttpServer server;

    public AwsApiProxyTestServer(ApplicationContext applicationContext,
                                 HttpServerConfiguration httpServerConfiguration) {
        this.applicationContext = applicationContext;
        this.handler = createLambdaHandler(applicationContext);
        this.serverPort = createServerPort(httpServerConfiguration);
    }

    private static APIGatewayV2HTTPEventFunction createLambdaHandler(ApplicationContext ctx) {
        ApplicationContextBuilder builder = ApplicationContext.builder();
        for (PropertySource propertySource : ctx.getEnvironment().getPropertySources()) {
            builder = builder.propertySources(propertySource);
        }
        return new APIGatewayV2HTTPEventFunction(builder.build());
    }

    private ServerPort createServerPort(HttpServerConfiguration httpServerConfiguration) {
        Optional<Integer> portOpt = httpServerConfiguration.getPort();
        if (portOpt.isPresent()) {
            Integer port = portOpt.get();
            if (port == -1) {
                return new ServerPort(true, 0);

            } else {
                return new ServerPort(false, port);
            }
        } else {
            if (applicationContext.getEnvironment().getActiveNames().contains(Environment.TEST)) {
                return new ServerPort(true, 0);
            } else {
                return new ServerPort(false, 8080);
            }
        }
    }

    @Override
    public EmbeddedServer start() {
        if (running.compareAndSet(false, true)) {
            int port = serverPort.getPort();
            try {
                server = HttpServer.create(new InetSocketAddress(port),0);
                server.createContext("/", new AwsProxyHandler(handler));
                server.start();
            } catch (Exception e) {
                throw new ServerStartupException(e.getMessage(), e);
            }
        }
        return this;
    }

    @Override
    public EmbeddedServer stop() {
        if (running.compareAndSet(true, false)) {
            try {
                server.stop(0);
            } catch (Exception e) {
                // ignore / unrecoverable
            }
        }
        return this;
    }

    @Override
    public int getPort() {
        return server.getAddress().getPort();
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
        return handler.getApplicationContext();
    }

    @Override
    public ApplicationConfiguration getApplicationConfiguration() {
        return getApplicationContext().getBean(ApplicationConfiguration.class);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private static class AwsProxyHandler implements HttpHandler {
        APIGatewayV2HTTPEventFunction handler;
        private final HttpExchangeToAwsProxyRequestAdapter requestAdapter;
        private final ConversionService conversionService;
        private final ContextProvider contextProvider;

        private AwsProxyHandler(APIGatewayV2HTTPEventFunction handler) {
            this.handler = handler;
            ApplicationContext ctx = handler.getApplicationContext();
            this.contextProvider = ctx.getBean(ContextProvider.class);
            this.requestAdapter = ctx.getBean(HttpExchangeToAwsProxyRequestAdapter.class);
            this.conversionService = ctx.getBean(ConversionService.class);
        }

        @Override
        public void handle(HttpExchange httpExchange) throws IOException {
            APIGatewayV2HTTPEvent awsProxyRequest = requestAdapter.createAwsProxyRequest(httpExchange);
            APIGatewayV2HTTPResponse apiGatewayV2HTTPResponse = handler.handleRequest(awsProxyRequest, contextProvider.getContext());
            String payload = apiGatewayV2HTTPResponse.getBody();
            String contentLengthObject = apiGatewayV2HTTPResponse.getHeaders().get(HttpHeaders.CONTENT_LENGTH);
            int contentLength = StringUtils.isNotEmpty(contentLengthObject) ? Integer.parseInt(contentLengthObject) : 0;
            for (String headerName : apiGatewayV2HTTPResponse.getHeaders().keySet()) {
                String headerValue = apiGatewayV2HTTPResponse.getHeaders().get(headerName);
                List<String> headerValues = List.of(headerValue.split(","));
                httpExchange.getResponseHeaders().put(headerName, StringUtils.isEmpty(headerValue) ? Collections.emptyList() : headerValues);
            }
            httpExchange.sendResponseHeaders(apiGatewayV2HTTPResponse.getStatusCode(), contentLength);
            if (StringUtils.isNotEmpty(payload)) {
                final OutputStream output = httpExchange.getResponseBody();
                byte[] payloadBytes = payload.getBytes();
                if (apiGatewayV2HTTPResponse.getIsBase64Encoded())  {
                    payloadBytes = Base64.getDecoder().decode(payloadBytes);
                }
                output.write(payloadBytes);
                output.flush();
            }
            httpExchange.close();
        }
    }
}
