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
import com.amazonaws.serverless.proxy.model.AwsProxyRequest;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.io.socket.SocketUtils;
import io.micronaut.function.aws.proxy.MicronautLambdaHandler;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.exceptions.HttpServerException;
import io.micronaut.http.server.exceptions.ServerStartupException;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.server.EmbeddedServer;
import org.eclipse.jetty.server.Request;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.handler.AbstractHandler;

import javax.inject.Singleton;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.net.BindException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
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
                return new ServerPort(true, SocketUtils.findAvailableTcpPort());

            } else {
                return new ServerPort(false, port);
            }
        } else {
            if (applicationContext.getEnvironment().getActiveNames().contains(Environment.TEST)) {
                return new ServerPort(true, SocketUtils.findAvailableTcpPort());
            } else {
                return new ServerPort(false, 8080);
            }
        }
    }

    @Override
    public EmbeddedServer start() {
        if (running.compareAndSet(false, true)) {
            int retryCount = 0;
            int port = serverPort.getPort();
            while (retryCount <= 3) {
                try {
                    this.server = new Server(port);
                    this.server.setHandler(new AwsProxyHandler());
                    this.server.start();
                    break;
                } catch (BindException e) {
                    if (serverPort.isRandom()) {
                        port = SocketUtils.findAvailableTcpPort();
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
        return serverPort.getPort();
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

        private final ServletToAwsProxyRequestAdapter requestAdapter;
        private final ServletToAwsProxyResponseAdapter responseAdapter;
        private final ContextProvider contextProvider;

        public AwsProxyHandler() throws ContainerInitializationException {
            lambdaHandler = new MicronautLambdaHandler();
            ApplicationContext ctx = lambdaHandler.getApplicationContext();
            this.requestAdapter = ctx.getBean(ServletToAwsProxyRequestAdapter.class);
            this.responseAdapter = ctx.getBean(ServletToAwsProxyResponseAdapter.class);
            this.contextProvider = ctx.getBean(ContextProvider.class);
        }

        @Override
        public void destroy() {
            super.destroy();
            this.lambdaHandler.close();
        }

        public void handle(String target, Request baseRequest, HttpServletRequest request, HttpServletResponse response)
                throws IOException {
            AwsProxyRequest awsProxyRequest = requestAdapter.createAwsProxyRequest(request);
            AwsProxyResponse awsProxyResponse = lambdaHandler.handleRequest(awsProxyRequest, contextProvider.getContext());
            responseAdapter.handle(request, awsProxyResponse, response);
        }
    }
}
