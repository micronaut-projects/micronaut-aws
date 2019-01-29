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
package io.micronaut.function.aws.proxy.model.factory;

import io.micronaut.function.aws.proxy.MicronautAwsProxyRequest;
import io.micronaut.function.aws.proxy.MicronautAwsProxyResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponseFactory;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.server.exceptions.InternalServerException;

import java.util.Optional;

/**
 * Implementation of {@link HttpResponseFactory} that looks up the current
 * {@link MicronautAwsProxyRequest} and uses that to construct the response.
 *
 * @author graemerocher
 * @since 1.1
 */
public class MicronautAwsProxyResponseFactory implements HttpResponseFactory {
    @SuppressWarnings("unchecked")
    @Override
    public <T> MutableHttpResponse<T> ok(T body) {
        final Optional<HttpRequest<Object>> request = ServerRequestContext.currentRequest();
        return request.map(req -> {
            if (req instanceof MicronautAwsProxyRequest) {
                final MicronautAwsProxyResponse<T> response = (MicronautAwsProxyResponse<T>) ((MicronautAwsProxyRequest<Object>) req).getResponse();
                return response.status(HttpStatus.OK).body(body);
            }
            throw new InternalServerException("No request present");
        }).orElseThrow(() -> new InternalServerException("No request present"));
    }

    @Override
    public <T> MutableHttpResponse<T> status(HttpStatus status, String reason) {
        final Optional<HttpRequest<Object>> request = ServerRequestContext.currentRequest();
        return request.map(req -> {
            if (req instanceof MicronautAwsProxyRequest) {
                final MicronautAwsProxyResponse<T> response = (MicronautAwsProxyResponse<T>) ((MicronautAwsProxyRequest<Object>) req).getResponse();
                return response.status(status, reason);
            }
            throw new InternalServerException("No request present");
        }).orElseThrow(() -> new InternalServerException("No request present"));
    }

    @Override
    public <T> MutableHttpResponse<T> status(HttpStatus status, T body) {
        final Optional<HttpRequest<Object>> request = ServerRequestContext.currentRequest();
        return request.map(req -> {
            if (req instanceof MicronautAwsProxyRequest) {
                final MicronautAwsProxyResponse<T> response = (MicronautAwsProxyResponse<T>) ((MicronautAwsProxyRequest<Object>) req).getResponse();
                return response.body(body).status(status);
            }
            throw new InternalServerException("No request present");
        }).orElseThrow(() -> new InternalServerException("No request present"));
    }
}
