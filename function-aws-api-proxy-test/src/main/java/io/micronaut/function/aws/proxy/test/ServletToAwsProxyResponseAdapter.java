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

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.core.annotation.NonNull;
import io.micronaut.context.annotation.DefaultImplementation;


import io.micronaut.core.convert.ConversionService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Writes the contents of a {@link APIGatewayV2HTTPResponse} to a {@link HttpServletResponse}.
 * @author Sergio del Amo
 */
@DefaultImplementation(DefaultServletToAwsProxyResponseAdapter.class)
@FunctionalInterface
public interface ServletToAwsProxyResponseAdapter {

    /**
     *
     * Writes the contents of a {@link APIGatewayV2HTTPResponse} to a {@link HttpServletResponse}.
     *
     * @param conversionService The conversion service
     * @param request Servlet Request
     * @param awsProxyResponse The AWS proxy response
     * @param response The Servlet Response
     * @throws IOException can be thrown while writing the response
     */
    void handle(@NonNull ConversionService conversionService,
                @NonNull HttpServletRequest request,
                @NonNull APIGatewayV2HTTPResponse awsProxyResponse,
                @NonNull HttpServletResponse response) throws IOException;
}
