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
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.ApplicationContextBuilder;
import io.micronaut.context.env.Environment;
import io.micronaut.context.env.PropertySource;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.function.aws.proxy.payload2.APIGatewayV2HTTPEventFunction;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.exceptions.HttpServerException;
import io.micronaut.http.server.exceptions.ServerStartupException;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.server.EmbeddedServer;
import jakarta.inject.Singleton;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
    private final ServerPort serverPort;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private Server server;

    public AwsApiProxyTestServer(ApplicationContext applicationContext,
                                 HttpServerConfiguration httpServerConfiguration) {
        this.applicationContext = applicationContext;
        this.serverPort = createServerPort(httpServerConfiguration);
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
                this.server = new Server(port);
                this.server.setHandler(new AwsProxyHandler(applicationContext));
                this.server.start();
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
                server.stop();
            } catch (Exception e) {
                // ignore / unrecoverable
            }
        }
        return this;
    }

    @Override
    public int getPort() {
        return server.getURI().getPort();
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
        // Return the applicationContext of the handler constructed below, not that of the test-server
        return ((AwsProxyHandler) server.getHandler()).getApplicationContext();
    }

    @Override
    public ApplicationConfiguration getApplicationConfiguration() {
        return getApplicationContext().getBean(ApplicationConfiguration.class);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }

    private static class AwsProxyHandler extends AbstractHandler {
        private static final Logger LOG = LoggerFactory.getLogger(AwsProxyHandler.class);

        private final APIGatewayV2HTTPEventFunction lambdaHandler;
        private final ServletToAwsProxyRequestAdapter requestAdapter;
        private final ServletToAwsProxyResponseAdapter responseAdapter;
        private final ConversionService conversionService;
        private final ContextProvider contextProvider;

        public AwsProxyHandler(ApplicationContext proxyTestApplicationContext) {
            ApplicationContextBuilder builder = ApplicationContext.builder();
            for (PropertySource propertySource : proxyTestApplicationContext.getEnvironment().getPropertySources()) {
                builder = builder.propertySources(propertySource);
            }
            lambdaHandler = new APIGatewayV2HTTPEventFunction(builder.build());
            ApplicationContext ctx = lambdaHandler.getApplicationContext();
            this.contextProvider = ctx.getBean(ContextProvider.class);
            this.requestAdapter = ctx.getBean(ServletToAwsProxyRequestAdapter.class);
            this.responseAdapter = ctx.getBean(ServletToAwsProxyResponseAdapter.class);
            this.conversionService = ctx.getBean(ConversionService.class);
        }

        ApplicationContext getApplicationContext() {
            return lambdaHandler.getApplicationContext();
        }

        @Override
        public void destroy() {
            super.destroy();
             this.lambdaHandler.close();
        }

        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response) throws IOException {
            APIGatewayV2HTTPEvent awsProxyRequest = requestAdapter.createAwsProxyRequest(request);
            APIGatewayV2HTTPResponse apiGatewayV2HTTPResponse = lambdaHandler.handleRequest(awsProxyRequest, contextProvider.getContext());
            responseAdapter.handle(conversionService, request, apiGatewayV2HTTPResponse, response);
            baseRequest.setHandled(true);
        }
    }
}
