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
package io.micronaut.aws.alexa.httpserver.exceptions;

import io.micronaut.context.annotation.Requires;
import io.micronaut.http.HttpRequest;
import io.micronaut.http.HttpResponse;
import io.micronaut.http.annotation.Produces;
import io.micronaut.http.hateoas.JsonError;
import io.micronaut.http.hateoas.Link;
import io.micronaut.http.server.exceptions.ExceptionHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.inject.Singleton;

/**
 * {@link ExceptionHandler} to handle {@link SecurityException}. It returns a 400 response with the exception message wrapped in a {@link JsonError}.
 * @author sdelamo
 * @since 2.0.0
 */
@Produces
@Singleton
@Requires(classes = {SecurityException.class})
public class SecurityExceptionHandler implements ExceptionHandler<SecurityException, HttpResponse> {

    private static final Logger LOG = LoggerFactory.getLogger(SecurityExceptionHandler.class);

    @Override
    public HttpResponse handle(HttpRequest request, SecurityException ex) {
        if (LOG.isErrorEnabled()) {
            LOG.error("Incoming request failed verification 400", ex);
        }
        JsonError error = new JsonError(ex.getMessage());
        error.link(Link.SELF, Link.of(request.getUri()));
        return HttpResponse.badRequest(error);
    }
}
