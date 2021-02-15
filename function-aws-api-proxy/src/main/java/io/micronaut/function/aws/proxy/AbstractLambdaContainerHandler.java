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


import com.amazonaws.serverless.exceptions.ContainerInitializationException;
import com.amazonaws.serverless.proxy.LogFormatter;
import com.amazonaws.serverless.proxy.internal.SecurityUtils;
import com.amazonaws.serverless.proxy.model.ContainerConfig;
import com.amazonaws.serverless.proxy.ExceptionHandler;
import com.amazonaws.serverless.proxy.RequestReader;
import com.amazonaws.serverless.proxy.ResponseWriter;
import com.amazonaws.serverless.proxy.SecurityContextWriter;
import com.amazonaws.services.lambda.runtime.Context;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.databind.ObjectWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.core.SecurityContext;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.concurrent.CountDownLatch;


/**
 * Abstract class that declares the basic methods and objects for implementations of <code>LambdaContainerHandler</code>.
 *
 * @param <RequestType> The expected request object. This is the model class that the event JSON is de-serialized to
 * @param <ResponseType> The expected Lambda function response object. Responses from the container will be written to this model object
 * @param <ContainerRequestType> The request type for the wrapped Java container
 * @param <ContainerResponseType> The response or response writer type for the wrapped Java container
 */
public abstract class AbstractLambdaContainerHandler<RequestType, ResponseType, ContainerRequestType, ContainerResponseType> {

    //-------------------------------------------------------------
    // Constants
    //-------------------------------------------------------------

    public static final String SERVER_INFO = "aws-serverless-java-container";

    //-------------------------------------------------------------
    // Variables - Private - Static
    //-------------------------------------------------------------

    private static ContainerConfig config = ContainerConfig.defaultConfig();

    protected Context lambdaContext;

    //-------------------------------------------------------------
    // Variables - Private
    //-------------------------------------------------------------
    private final RequestReader<RequestType, ContainerRequestType> requestReader;
    private final ResponseWriter<ContainerResponseType, ResponseType> responseWriter;
    private final SecurityContextWriter<RequestType> securityContextWriter;
    private final ExceptionHandler<ResponseType> exceptionHandler;
    private final Class<RequestType> requestTypeClass;
    private final Class<ResponseType> responseTypeClass;

    private LogFormatter<ContainerRequestType, ContainerResponseType> logFormatter;

    private final Logger log = LoggerFactory.getLogger(AbstractLambdaContainerHandler.class);

    //-------------------------------------------------------------
    // Constructors
    //-------------------------------------------------------------

    /**
     * Default constructor.
     *
     * @param requestClass The request class
     * @param responseClass The response class
     * @param requestReader The request reader
     * @param responseWriter The response writer
     * @param securityContextWriter The security context writer
     * @param exceptionHandler The exception handler
     */
    protected AbstractLambdaContainerHandler(Class<RequestType> requestClass,
                                     Class<ResponseType> responseClass,
                                     RequestReader<RequestType, ContainerRequestType> requestReader,
                                     ResponseWriter<ContainerResponseType, ResponseType> responseWriter,
                                     SecurityContextWriter<RequestType> securityContextWriter,
                                     ExceptionHandler<ResponseType> exceptionHandler) {
        log.info("Starting Lambda Container Handler");
        requestTypeClass = requestClass;
        responseTypeClass = responseClass;
        this.requestReader = requestReader;
        this.responseWriter = responseWriter;
        this.securityContextWriter = securityContextWriter;
        this.exceptionHandler = exceptionHandler;
        config.addBinaryContentTypes("application/zip");
    }

    //-------------------------------------------------------------
    // Methods - Abstract
    //-------------------------------------------------------------

    /**
     * The object mapper.
     *
     * @return Return the object mapper.
     */
    protected abstract ObjectMapper objectMapper();

    /**
     * Gets a writer for the given response class.
     * @param responseClass The response class
     * @return The writer
     */
    protected abstract ObjectWriter writerFor(Class<ResponseType> responseClass);

    /**
     * Gets a reader for the given request class.
     * @param requestClass The request class
     * @return The reader
     */
    protected abstract ObjectReader readerFor(Class<RequestType> requestClass);

    /**
     * Get the container response.
     * @param request The request
     * @param latch The count down latch
     * @return The response
     */
    protected abstract ContainerResponseType getContainerResponse(ContainerRequestType request, CountDownLatch latch);

    /**
     * Handle the request.
     * @param containerRequest The container request
     * @param containerResponse The response
     * @param lambdaContext The lambda context
     * @throws Exception The exception
     */
    protected abstract void handleRequest(ContainerRequestType containerRequest, ContainerResponseType containerResponse, Context lambdaContext)
            throws Exception;

    /**
     * Initialize the container.
     * @throws ContainerInitializationException when an error occurs
     */
    public abstract void initialize()
            throws ContainerInitializationException;

    /**
     * Configures the library to strip a base path from incoming requests before passing them on to the wrapped
     * framework. This was added in response to issue #34 (https://github.com/awslabs/aws-serverless-java-container/issues/34).
     * When creating a base path mapping for custom domain names in API Gateway we want to be able to strip the base path
     * from the request - the underlying service may not recognize this path.
     * @param basePath The base path to be stripped from the request
     */
    public void stripBasePath(String basePath) {
        if (basePath == null || "".equals(basePath)) {
            config.setStripBasePath(false);
            config.setServiceBasePath(null);
        } else {
            config.setStripBasePath(true);
            config.setServiceBasePath(basePath);
        }
    }

    /**
     * Sets the formatter used to log request data in CloudWatch. By default this is set to use an Apache
     * combined log format based on the servlet request and response object {@link com.amazonaws.serverless.proxy.internal.servlet.ApacheCombinedServletLogFormatter}.
     * @param formatter The log formatter object
     */
    public void setLogFormatter(LogFormatter<ContainerRequestType, ContainerResponseType> formatter) {
        this.logFormatter = formatter;
    }

    /**
     * Proxies requests to the underlying container given the incoming Lambda request. This method returns a populated
     * return object for the Lambda function.
     *
     * @param request The incoming Lambda request
     * @param context The execution context for the Lambda function
     * @return A valid response type
     */
    public ResponseType proxy(RequestType request, Context context) {
        lambdaContext = context;
        try {
            SecurityContext securityContext = securityContextWriter.writeSecurityContext(request, context);
            CountDownLatch latch = new CountDownLatch(1);
            ContainerRequestType containerRequest = requestReader.readRequest(request, securityContext, context, config);
            ContainerResponseType containerResponse = getContainerResponse(containerRequest, latch);

            handleRequest(containerRequest, containerResponse, context);

            latch.await();

            if (logFormatter != null && log.isInfoEnabled()) {
                log.info(SecurityUtils.crlf(logFormatter.format(containerRequest, containerResponse, securityContext)));
            }

            return responseWriter.writeResponse(containerResponse, context);
        } catch (Exception e) {
            log.error("Error while handling request", e);

            return exceptionHandler.handle(e);
        }
    }

    /**
     * Handles Lambda <code>RequestStreamHandler</code> method. The method uses an <code>ObjectMapper</code>
     * to transform the incoming input stream into the given {@link RequestType} and then calls the
     * {@link #proxy(Object, Context)} method to handle the request. The output from the proxy method is
     * written on the given output stream.
     * @param input Lambda's incoming input stream
     * @param output Lambda's response output stream
     * @param context Lambda's context object
     * @throws IOException If an error occurs during the stream processing
     */
    public void proxyStream(InputStream input, OutputStream output, Context context)
            throws IOException {

        try {
            RequestType request = readerFor(requestTypeClass).readValue(input);
            ResponseType resp = proxy(request, context);

            writerFor(responseTypeClass).writeValue(output, resp);
        } catch (JsonParseException e) {
            log.error("Error while parsing request object stream", e);
            objectMapper().writeValue(output, exceptionHandler.handle(e));
        } catch (JsonMappingException e) {
            log.error("Error while mapping object to RequestType class", e);
            objectMapper().writeValue(output, exceptionHandler.handle(e));
        } finally {
            output.flush();
            output.close();
        }
    }

    //-------------------------------------------------------------
    // Methods - Getter/Setter
    //-------------------------------------------------------------
    /**
     * Returns the current container configuration object.
     * @return The container configuration object
     */
    public static ContainerConfig getContainerConfig() {
        return config;
    }
}
