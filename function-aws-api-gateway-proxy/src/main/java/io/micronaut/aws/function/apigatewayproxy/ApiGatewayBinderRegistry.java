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
package io.micronaut.aws.function.apigatewayproxy;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import io.micronaut.context.annotation.Replaces;
import io.micronaut.core.annotation.Internal;
import io.micronaut.core.convert.ConversionService;
import io.micronaut.http.bind.DefaultRequestBinderRegistry;
import io.micronaut.http.bind.binders.RequestArgumentBinder;
import io.micronaut.http.codec.MediaTypeCodecRegistry;
import io.micronaut.servlet.http.ServletBinderRegistry;
import jakarta.inject.Singleton;

import java.util.List;

/**
 * Implementation of {@link ServletBinderRegistry} for AWS Gateway Proxy Events.
 *
 * @author Tim Yates
 * @since 4.0.0
 */
@Singleton
@Internal
@Replaces(DefaultRequestBinderRegistry.class)
public class ApiGatewayBinderRegistry extends ServletBinderRegistry {

    public ApiGatewayBinderRegistry(
        MediaTypeCodecRegistry mediaTypeCodecRegistry,
        ConversionService conversionService,
        List<RequestArgumentBinder> binders
    ) {
        super(mediaTypeCodecRegistry, conversionService, binders);
        this.byType.put(APIGatewayProxyRequestEvent.class, new ApiGatewayRequestBinder());
        this.byType.put(APIGatewayProxyResponseEvent.class, new ApiGatewayResponseBinder());
    }
}
