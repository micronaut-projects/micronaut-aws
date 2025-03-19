/*
 * Copyright 2017-2025 original authors
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

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpsServer;
import io.micronaut.context.ApplicationContext;
import io.micronaut.context.env.CachedEnvironment;
import io.micronaut.context.env.Environment;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.io.socket.SocketUtils;
import io.micronaut.http.server.HttpServerConfiguration;
import io.micronaut.http.server.exceptions.HttpServerException;
import io.micronaut.http.server.exceptions.ServerStartupException;
import io.micronaut.runtime.ApplicationConfiguration;
import io.micronaut.runtime.server.EmbeddedServer;

import java.net.*;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicBoolean;

@Internal
class HttpServerEmbeddedServer implements EmbeddedServer {
    private static final String SCHEME_HTTP = "http";
    private final HttpServerConfiguration httpServerConfiguration;
    private final AtomicBoolean running = new AtomicBoolean(false);
    private HttpServer server;
    private final HttpHandlerApplicationContextAware httpHandler;

    HttpServerEmbeddedServer(HttpHandlerApplicationContextAware httpHandler,
                             HttpServerConfiguration httpServerConfiguration) {
        this.httpServerConfiguration = httpServerConfiguration;
        this.httpHandler = httpHandler;
    }

    @Override
    public EmbeddedServer start() {
        if (running.compareAndSet(false, true)) {
            ServerPort serverPort = ServerPort.of(httpServerConfiguration, httpHandler.getApplicationContext().getEnvironment().getActiveNames());
            try {
                server = HttpServer.create(new InetSocketAddress(serverPort.port()), 0);
                server.createContext("/", httpHandler);
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
        return httpServerConfiguration.getHost()
                .orElseGet(() -> Optional.ofNullable(CachedEnvironment.getenv(Environment.HOSTNAME)).orElse(SocketUtils.LOCALHOST));
    }

    @Override
    public String getScheme() {
        return SCHEME_HTTP;
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
        return httpHandler.getApplicationContext();
    }

    @Override
    public ApplicationConfiguration getApplicationConfiguration() {
        return getApplicationContext().getBean(ApplicationConfiguration.class);
    }

    @Override
    public boolean isRunning() {
        return running.get();
    }
}
