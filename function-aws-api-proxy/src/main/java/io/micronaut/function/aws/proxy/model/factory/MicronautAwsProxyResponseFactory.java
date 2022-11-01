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
package io.micronaut.function.aws.proxy.model.factory;

import io.micronaut.core.io.service.ServiceDefinition;
import io.micronaut.core.io.service.SoftServiceLoader;
import io.micronaut.function.aws.proxy.MicronautAwsProxyRequest;
import io.micronaut.function.aws.proxy.MicronautAwsProxyResponse;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponseFactory;
import io.micronaut.http.HttpStatus;
import io.micronaut.http.MutableHttpResponse;
import io.micronaut.http.context.ServerRequestContext;
import io.micronaut.http.server.exceptions.InternalServerException;

/**
 * Implementation of {@link HttpResponseFactory} that looks up the current
 * {@link MicronautAwsProxyRequest} and uses that to construct the response.
 *
 * @author graemerocher
 * @since 1.1
 */
public class MicronautAwsProxyResponseFactory implements HttpResponseFactory {

    private static final HttpResponseFactory ALTERNATE;

    static {
        final SoftServiceLoader<HttpResponseFactory> factories = SoftServiceLoader.load(HttpResponseFactory.class);
        HttpResponseFactory alternate = null;
        for (ServiceDefinition<HttpResponseFactory> factory : factories) {
            if (factory.isPresent() && !factory.getName().equals(MicronautAwsProxyResponseFactory.class.getName())) {
                alternate = factory.load();
                break;
            }
        }

        ALTERNATE = alternate;
    }

    @SuppressWarnings("unchecked")
    @Override
    public <T> MutableHttpResponse<T> ok(T body) {
        final HttpRequest<Object> req = ServerRequestContext.currentRequest().orElse(null);
        if (req instanceof MicronautAwsProxyRequest) {
            final MicronautAwsProxyResponse<T> response = (MicronautAwsProxyResponse<T>) ((MicronautAwsProxyRequest<Object>) req).getResponse();
            return response.status(HttpStatus.OK).body(body);
        } else {
            if (ALTERNATE != null) {
                return ALTERNATE.ok(body);
            } else {
                throw new InternalServerException("No request present");
            }
        }
    }

    @Override
    public <T> MutableHttpResponse<T> status(HttpStatus status, String reason) {
        return status(status.getCode(), reason);
    }

    @Override
    public <T> MutableHttpResponse<T> status(int status, String reason) {
        final HttpRequest<Object> req = ServerRequestContext.currentRequest().orElse(null);
        if (req instanceof MicronautAwsProxyRequest) {
            final MicronautAwsProxyResponse<T> response = (MicronautAwsProxyResponse<T>) ((MicronautAwsProxyRequest<Object>) req).getResponse();
            return response.status(status, reason);
        } else {
            if (ALTERNATE != null) {
                return ALTERNATE.status(status, reason);
            } else {
                throw new InternalServerException("No request present");
            }
        }
    }

    @Override
    public <T> MutableHttpResponse<T> status(HttpStatus status, T body) {
        final HttpRequest<Object> req = ServerRequestContext.currentRequest().orElse(null);
        if (req instanceof MicronautAwsProxyRequest) {
            final MicronautAwsProxyResponse<T> response = (MicronautAwsProxyResponse<T>) ((MicronautAwsProxyRequest<Object>) req).getResponse();
            return response.body(body).status(status);
        } else {
            if (ALTERNATE != null) {
                return ALTERNATE.status(status, body);
            } else {
                throw new InternalServerException("No request present");
            }
        }
    }
}
