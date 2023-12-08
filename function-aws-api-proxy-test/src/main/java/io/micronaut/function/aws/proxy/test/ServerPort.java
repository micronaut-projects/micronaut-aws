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

/**
 * Encapsulates the port assignment to be used when starting a server.
 *
 * @author Sergio del Amo
 */
public class ServerPort {
    private boolean random;
    private Integer port;

    /**
     * Constructor.
     */
    public ServerPort() {
    }

    /**
     *
     * @param random Whether the port was randomly assigned
     * @param port Port number
     */
    public ServerPort(boolean random, Integer port) {
        this.random = random;
        this.port = port;
    }

    /**
     *
     * @return Whether the port was randomly assigned
     */
    public boolean isRandom() {
        return random;
    }

    /**
     *
     * @param random true if the port was randomly assigned
     */
    public void setRandom(boolean random) {
        this.random = random;
    }

    /**
     *
     * @return The port number
     */
    public Integer getPort() {
        return port;
    }

    /**
     *
     * @param port Port number
     */
    public void setPort(Integer port) {
        this.port = port;
    }
}
