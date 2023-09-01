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
package io.micronaut.function.aws.proxy.payload1;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.function.aws.proxy.BinaryContentConfiguration;
import io.micronaut.servlet.http.BodyBuilder;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpHandler;
import jakarta.inject.Singleton;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Implementation of {@link ServletHttpHandler} for AWS Gateway Proxy Events.
 *
 * @author Tim Yates
 * @since 4.0.0
 */
@Internal
@Singleton
public class ApiGatewayProxyEventHandler extends ServletHttpHandler<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> {
    private static final Logger LOG = LoggerFactory.getLogger(ApiGatewayProxyEventHandler.class);

    public ApiGatewayProxyEventHandler(ApplicationContext applicationContext) {
        super(applicationContext, applicationContext.getBean(ConversionService.class));
    }

    @Override
    protected ServletExchange<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> createExchange(
        APIGatewayProxyRequestEvent request,
        APIGatewayProxyResponseEvent response
    ) {
        if (LOG.isTraceEnabled()) {
            LOG.info("Request: {}", request);
        }
        return new ApiGatewayProxyServletRequest<>(
            request,
            new ApiGatewayProxyServletResponse<>(
                getApplicationContext().getConversionService(),
                getApplicationContext().getBean(BinaryContentConfiguration.class)
            ),
            applicationContext.getConversionService(),
            applicationContext.getBean(BodyBuilder.class)
        );
    }
}
