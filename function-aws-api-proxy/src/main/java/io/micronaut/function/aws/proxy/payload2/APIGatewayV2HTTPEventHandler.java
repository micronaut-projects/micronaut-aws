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
package io.micronaut.function.aws.proxy.payload2;

import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayV2HTTPResponse;
import io.micronaut.context.ApplicationContext;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.servlet.http.ServletExchange;
import io.micronaut.servlet.http.ServletHttpHandler;
import jakarta.inject.Singleton;

/**
 * Implementation of {@link ServletHttpHandler} for AWS Gateway Proxy Events.
 *
 * @author Tim Yates
 * @since 4.0.0
 */
@Internal
@Singleton
public class APIGatewayV2HTTPEventHandler extends ServletHttpHandler<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> {

    public APIGatewayV2HTTPEventHandler(ApplicationContext applicationContext) {
        super(applicationContext, applicationContext.getBean(ConversionService.class));
    }

    @Override
    protected ServletExchange<APIGatewayV2HTTPEvent, APIGatewayV2HTTPResponse> createExchange(
        APIGatewayV2HTTPEvent request,
        APIGatewayV2HTTPResponse response
    ) {
        return new APIGatewayV2HTTPEventServletRequest<>(
            request,
            new APIGatewayV2HTTPResponseServletResponse<>(getApplicationContext().getConversionService()),
            getMediaTypeCodecRegistry(),
            applicationContext.getConversionService()
        );
    }
}
