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
package io.micronaut.function.aws.proxy;

import com.amazonaws.serverless.proxy.AwsProxyExceptionHandler;
import com.amazonaws.serverless.proxy.ExceptionHandler;
import com.amazonaws.serverless.proxy.model.AwsProxyResponse;
import com.amazonaws.serverless.proxy.model.ErrorModel;
import com.amazonaws.serverless.proxy.model.Headers;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

/**
 * Default {@link ExceptionHandler} implementation.
 *
 * @author graemerocher
 * @since 1.1
 */
public class MicronautAwsProxyExceptionHandler implements ExceptionHandler<AwsProxyResponse>  {
    //-------------------------------------------------------------
    // Constants
    //-------------------------------------------------------------
    private static final Logger LOG = LoggerFactory.getLogger(AwsProxyExceptionHandler.class);
    private static final String INTERNAL_SERVER_ERROR = "Internal Server Error";
    //-------------------------------------------------------------
    // Variables - Private - Static
    //-------------------------------------------------------------
    private static Headers headers = new Headers();

    static {
        headers.putSingle(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON);
    }

    private final MicronautLambdaContainerContext environment;

    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------
    /**
     * Default constructor.
     * @param environment The environment.
     */
    protected MicronautAwsProxyExceptionHandler(MicronautLambdaContainerContext environment) {
        this.environment = environment;
    }

    //-------------------------------------------------------------
    // Implementation - ExceptionHandler
    //-------------------------------------------------------------
    @Override
    public AwsProxyResponse handle(Throwable ex) {
        LOG.error("Called exception handler for:", ex);

        // adding a print stack trace in case we have no appender or we are running inside SAM local, where need the
        // output to go to the stderr.
        ex.printStackTrace();
        return new AwsProxyResponse(500, headers, getErrorJson(INTERNAL_SERVER_ERROR));
    }

    @Override
    public void handle(Throwable ex, OutputStream stream) throws IOException {
        AwsProxyResponse response = handle(ex);

        environment
                .getObjectMapper()
                .writeValue(stream, response);
    }

    //-------------------------------------------------------------
    // Methods - Protected
    //-------------------------------------------------------------
    /**
     * Get the error JSON.
     * @param message The message
     * @return The error json
     */
    protected String getErrorJson(String message) {
        try (ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            environment
                    .getObjectMapper()
                    .writeValue(baos, new ErrorModel(message));
            return baos.toString(StandardCharsets.UTF_8.name());
        } catch (IOException e) {
            LOG.error("Could not produce error JSON", e);
            return "{ \"message\": \"" + message + "\" }";
        }
    }
}
